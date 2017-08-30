package com.basilgregory.finotesormsample;

import com.basilgregory.finotes_orm.annotations.Table;
import com.basilgregory.finotes_orm.builder.Entity;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by donpeter on 8/28/17.
 */
@Table(name = "events")
public class Event extends Entity {
    private String name;
    private Integer integer;
    private BigDecimal bigDecimal;
    private BigInteger bigInteger;
    private int anInt;
    private float aFloat;
    private double aDouble;
    private long aLong;

    private Issue issue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    /**
     * Notes fetch the event using foriegn key when getEvent is called.
     * Column fields needed for existing projects integration.
     *
     *
     * @return
     */

//    @OneToOne(column = "")
//    public Issue getIssue() {

//        return (Issue) super.getterCalled(Issue.class);
//    }

    public void setIssue(Issue event) {
        this.issue = event;
    }
}
