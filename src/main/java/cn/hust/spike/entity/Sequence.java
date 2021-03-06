package cn.hust.spike.entity;

import java.io.Serializable;

public class Sequence implements Serializable {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sequence.name
     *
     * @mbggenerated
     */
    private String name;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sequence.current_value
     *
     * @mbggenerated
     */
    private Integer currentValue;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sequence.step
     *
     * @mbggenerated
     */
    private Integer step;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table sequence
     *
     * @mbggenerated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sequence.name
     *
     * @return the value of sequence.name
     *
     * @mbggenerated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sequence.name
     *
     * @param name the value for sequence.name
     *
     * @mbggenerated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sequence.current_value
     *
     * @return the value of sequence.current_value
     *
     * @mbggenerated
     */
    public Integer getCurrentValue() {
        return currentValue;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sequence.current_value
     *
     * @param currentValue the value for sequence.current_value
     *
     * @mbggenerated
     */
    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sequence.step
     *
     * @return the value of sequence.step
     *
     * @mbggenerated
     */
    public Integer getStep() {
        return step;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sequence.step
     *
     * @param step the value for sequence.step
     *
     * @mbggenerated
     */
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence
     *
     * @mbggenerated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", name=").append(name);
        sb.append(", currentValue=").append(currentValue);
        sb.append(", step=").append(step);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}