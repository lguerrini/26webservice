package com.example.webservice.iot;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_iot")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(length = 245)
    private String description;

    @Column(name = "macaddress", nullable = false, length = 45)
    private String macaddress;

    @Column(name = "jsonrange", length = 512)
    private String jsonrange;

    @Column(name = "daterev")
    private LocalDateTime daterev;

    protected Item() {
    }

    public Item(String name, String description, String macaddress, String jsonrange) {
        this.name = name;
        this.description = description;
        this.macaddress = macaddress;
        this.jsonrange = jsonrange;
        this.daterev = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getJsonrange() {
        return jsonrange;
    }

    public void setJsonrange(String jsonrange) {
        this.jsonrange = jsonrange;
    }

    public LocalDateTime getDaterev() {
        return daterev;
    }

    public void setDaterev(LocalDateTime daterev) {
        this.daterev = daterev;
    }
}