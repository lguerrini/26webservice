package com.example.webservice.data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "t_data")
public class IotData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_iot", nullable = false)
    private Integer idIot;

    @Generated(event = EventType.INSERT)
    @Column(name = "datetime", insertable = false, updatable = false)
    private LocalDateTime datetime;

    @Column(name = "jsondata", length = 120)
    private String jsondata;

    protected IotData() {
    }

    public IotData(Integer idIot, String jsondata) {
        this.idIot = idIot;
        this.jsondata = jsondata;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdIot() {
        return idIot;
    }

    public void setIdIot(Integer idIot) {
        this.idIot = idIot;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public String getJsondata() {
        return jsondata;
    }

    public void setJsondata(String jsondata) {
        this.jsondata = jsondata;
    }
}