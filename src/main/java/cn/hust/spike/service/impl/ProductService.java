package cn.hust.spike.service.impl;

import cn.hust.spike.common.Const;
import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.converter.Product2ProductDTO;
import cn.hust.spike.converter.ProductDTO2Product;
import cn.hust.spike.dao.ProductMapper;
import cn.hust.spike.dao.ProductStockMapper;
import cn.hust.spike.dao.StockLogMapper;
import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.Product;
import cn.hust.spike.entity.ProductStock;
import cn.hust.spike.entity.StockLog;
import cn.hust.spike.mq.MQProducer;
import cn.hust.spike.service.IProductSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:22
 **/
@Service
public class ProductService implements IProductSevice{

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private StockLogMapper stockLogMapper;


    /**
     * 创建产品
     * @param productDTO
     * @return
     */
    @Transactional
    @Override
    public ServerResponse createProduct(ProductDTO productDTO){

        //插入商品到数据库
        Product product = ProductDTO2Product.conver(productDTO);
        productMapper.insertSelective(product);

        //插入商品库存到数据库
        ProductStock productStock = new ProductStock();
        productStock.setProductId(product.getId());
        productStock.setStock(productDTO.getStock());
        productStockMapper.insertSelective(productStock);

        return ServerResponse.createBySuccess();

    }


    /**
     * 查询产品详情
     * @param id
     * @return
     */
    public ServerResponse<ProductDTO> productDetail(Integer id){

        Product product = productMapper.selectByPrimaryKey(id);
        if(product == null){
            return ServerResponse.createByErrorMessage("该商品不存在");
        }

        //查看对应商品是否是秒杀商品
        PromoDTO promoDTO = promoService.getPromoByProductId(id);


        ProductStock productStock = productStockMapper.selectByProductId(product.getId());

        ProductDTO productDTO = Product2ProductDTO.conver(product,productStock,promoDTO);
        productDTO.setId(id);

        return ServerResponse.createBySuccess(productDTO);


    }

    /**
     * 商品列表
     * @return
     */
    public ServerResponse productList(){

        List<Product> products = productMapper.selectProductList();

        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductStock productStock = productStockMapper.selectByProductId(product.getId());
            ProductDTO productDTO = Product2ProductDTO.conver(product, productStock,null);
            return productDTO;
        }).collect(Collectors.toList());

        return ServerResponse.createBySuccess(productDTOList);

    }


    /**
     * 从redis中查询商品
     * @param productId
     * @return
     */
    public Product selectProductCacheById(Integer productId){
        Product product = (Product)redisTemplate.opsForValue().get("ProductCache"+ productId);
        if(product == null){
            product = productMapper.selectByPrimaryKey(productId);
            redisTemplate.opsForValue().set("ProductCache"+ productId,product);
        }

        return product;

    }


    /**
     * 扣减库存
     * @param productId
     * @param amount
     * @return
     */
    @Transactional
    public boolean decreaseStock(Integer productId , Integer amount ){
        //第一版  直接更新数据库中的内存 但会导致数据库行锁，效率慢
//        int affectRow = productStockMapper.decreaseStock(productId, amount);
//        if(affectRow > 0){
//            return true;
//        }else {
//            return false;
//        }

        long result = redisTemplate.opsForValue().increment("promo_product_stock"+ productId,amount.intValue() * -1);

        //result代表剩下的库存  大于0表示库存足够
        if(result >0){

            //异步发送扣减库存消息给数据库消费者 flag表示消息是否发送成功 ,
            //这样做是有问题的，假设事务在之后的处理中例如订单入库事务回滚了，但是这条消息已经投递出去了，会导致少卖
            //解决的第一个想法是在该事务最后一步投递消息，但还是有问题，因为事务提交是在函数全部执行完，所以即使你投递消息在最后
            //也可能会导致消息投递出去但是事务提交失败导致回滚
            //那么可以在spring提供的事务方法中在事务提交后再投递消息，但是投递消息可能会失败，说白了就是分布式事务的问题
//            boolean mqResult = mqProducer.asyncStockReduceMessage(productId, amount);
//            if(mqResult){
//                return true;
//            }else {
//                //发送消息失败需要把redis扣减的库存给加回来
//                redisTemplate.opsForValue().increment("promo_product_stock"+ productId,amount.intValue());
//                return false;
//            }


            return true;
        }else if(result == 0){
            //打上库存售罄标识
            redisTemplate.opsForValue().set("promo_stock_invalid_"+productId,"true");
            return true;

        }else{
            //redis扣减库存不足需要把redis扣减的库存给加回来
            increaseStock(productId,amount);
            return false;
        }


    }



    @Transactional
    public void  increaseStock(Integer productId , Integer amount ){

        redisTemplate.opsForValue().increment("promo_product_stock"+ productId,amount.intValue());

    }


      public boolean asyncStockReduceMessage(Integer productId,Integer amount){

           return  mqProducer.asyncStockReduceMessage(productId, amount);

    }


    /**
     * 初始化库存流水状态，便于本地事务反查
     * @param productId
     * @param amount
     * @return
     */
      public String initStockStatus(Integer productId, Integer amount){

          StockLog stockLog = new StockLog();
          stockLog.setStockLogId(UUID.randomUUID().toString());
          stockLog.setAmount(amount);
          stockLog.setProductId(productId);
          stockLog.setStatus(Const.Stock_Status.INIT_STOCK_STATUS.getCode());

          stockLogMapper.insertSelective(stockLog);
          return stockLog.getStockLogId();


      }


      @Transactional
      public void increaseSales(Integer productId,Integer amount){

          //存在行锁
         productMapper.increaseSales(productId,amount);
      }
}
