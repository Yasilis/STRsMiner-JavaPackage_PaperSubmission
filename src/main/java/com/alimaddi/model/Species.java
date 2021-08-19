package com.alimaddi.model;


import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author Ali Maddi
 */
//TODO : change annotation to the org.hibernate.annotations!
@Entity
@Table(
        name = "Species",
        indexes =
                {@Index(columnList = "name", name = "species_name_idx"),
                 @Index(columnList = "checked_1", name = "species_checked_1_idx"),
                 @Index(columnList = "checked_2", name = "species_checked_2_idx")},
        uniqueConstraints =
                {@UniqueConstraint(columnNames = "name", name = "species_name_unq")})
public class Species implements Serializable
{

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "name", nullable = false, columnDefinition = "varchar(50)")
    private String name;

    @Column(name = "common_name", columnDefinition = "varchar(50)")
    private String commonName;

    @Column(name = "display_name", columnDefinition = "varchar(50)")
    private String displayName;

    @Column(name = "last_update_time", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastUpdateTime;

    @Column(name = "checked_1", nullable = false)
    @ColumnDefault("0")
    private short checked1;

    @Column(name = "checked_2", nullable = false)
    @ColumnDefault("0")
    private short checked2;

    @OneToMany(mappedBy = "species", cascade = CascadeType.PERSIST)
    private final Set<Gene> genesSet;

    public Species()
    {
        genesSet = new HashSet<>();
    }

    public Species(String name, String commonName, String displayName, Date updateTime)
    {
        this();
        this.name = name;
        this.commonName = commonName;
        this.displayName = displayName;
        this.lastUpdateTime = updateTime;
    }

    public Species(String name, String commonName, String displayName, Date updateTime, short checked1)
    {
        this(name, commonName, displayName, updateTime);
        setChecked1(checked1);
    }

    public Species(String name, String commonName, String displayName, Date updateTime, short checked1, short checked2)
    {
        this(name, commonName, displayName, updateTime, checked1);
        setChecked2(checked2);
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public Date getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime)
    {
        this.lastUpdateTime = lastUpdateTime;
    }

    public short getChecked1()
    {
        return checked1;
    }

    public void setChecked1(short checked1)
    {
        this.checked1 = checked1;
    }

    public short getChecked2()
    {
        return checked2;
    }

    public void setChecked2(short checked2)
    {
        this.checked2 = checked2;
    }

    public Set<Gene> getGenesSet()
    {
        return genesSet;
    }

    public void addGene(Gene gene)
    {
        this.genesSet.add(gene);
    }

    public void addAllGenes(Collection<Gene> genes)
    {
        this.genesSet.addAll(genes);
    }

    @Override
    public int hashCode()
    {
        // Caution there is some record with equal name but different commonName and displayName!
        // So we don't use commonName and displayName in equality or hashing
        return Objects.hash(this.name);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (obj == this)
            return true;

        Species species = (Species) obj;
        // Caution there is some record with equal name but different commonName and displayName!
        // So we don't use commonName and displayName in equality or hashing
        return (this.name.equals(species.getName()));
    }

    @Override
    public String toString()
    {
        return "Species{" + "id=" + id + ", name='" + name + '\'' + ", commonName='" + commonName + '\'' + ", displayName='" + displayName + '\'' + ", lastUpdateTime=" + lastUpdateTime + ", checked1=" + checked1 + ", checked2=" + checked2 + '}';
    }
}
