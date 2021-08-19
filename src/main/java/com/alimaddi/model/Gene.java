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
        name = "Gene",
        indexes =
                {@Index(columnList = "gene_stable_id", name = "gene_gene_stable_id_idx"),
                 @Index(columnList = "checked_1", name = "gene_checked_1_idx"),
                 @Index(columnList = "checked_2", name = "gene_checked_2_idx")},
        uniqueConstraints =
                {@UniqueConstraint(columnNames = "gene_stable_id", name = "gene_gene_stable_id_unq")})
public class Gene implements Serializable
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "gene_stable_id", nullable = false, columnDefinition = "varchar(25)")
    private String geneStableID;

    @Column(name = "gene_type", nullable = false, columnDefinition = "varchar(25)")
    private String type;

    @Column(name = "gene_name", columnDefinition = "varchar(40)")
    private String geneName;

    @Column(name = "last_update_time", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastUpdateTime;

    @Column(name = "checked_1", nullable = false)
    @ColumnDefault("0")
    private short checked1;

    @Column(name = "checked_2", nullable = false)
    @ColumnDefault("0")
    private short checked2;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "species_id", referencedColumnName = "id")
    private Species species;

    @OneToMany(mappedBy = "gene", cascade = CascadeType.PERSIST)
    private final Set<Transcript> transcriptsSet;

    public Gene()
    {
        transcriptsSet = new HashSet<>();
    }

    public Gene(String geneStableID, String type, String geneName, Date updateTime, Species species)
    {
        this();
        this.geneStableID = geneStableID;
        this.type = type;
        this.geneName = geneName;
        this.lastUpdateTime = updateTime;
        this.species = species;
    }

    public Gene(String geneStableID, String type, String geneName, Date updateTime,
                Species species, short checked1)
    {
        this(geneStableID, type, geneName, updateTime, species);
        setChecked1(checked1);
    }
    public Gene(String geneStableID, String type, String geneName, Date updateTime,
                Species species, short checked1, short checked2)
    {
        this(geneStableID, type, geneName, updateTime, species, checked1);
        setChecked2(checked2);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getGeneStableID()
    {
        return geneStableID;
    }

    public void setGeneStableID(String geneStableID)
    {
        this.geneStableID = geneStableID;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String geneType)
    {
        this.type = geneType;
    }

    public String getGeneName()
    {
        return geneName;
    }

    public void setGeneName(String geneName)
    {
        this.geneName = geneName;
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

    public Species getSpecies()
    {
        return species;
    }

    public void setSpecies(Species species)
    {
        this.species = species;
    }

    public Set<Transcript> getTranscriptsSet()
    {
        return transcriptsSet;
    }

    public void addTranscript(Transcript transcript)
    {
        this.transcriptsSet.add(transcript);
    }

    public void addAllTranscripts(Collection<Transcript> transcripts)
    {
        this.transcriptsSet.addAll(transcripts);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(geneStableID, species.getId(), type);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (this == obj)
            return true;

        Gene gene = (Gene) obj;
        return Objects.equals(geneStableID, gene.geneStableID) &&
                Objects.equals(type, gene.type) &&
                Objects.equals(species, gene.species);
    }

    @Override
    public String toString()
    {
        return "Gene{" + "id=" + id + ", geneStableID='" + geneStableID + '\'' + ", geneType='" + type + '\'' + ", geneName='" + geneName + '\'' + ", lastUpdateTime=" + lastUpdateTime + ", checked1=" + checked1 + ", checked2=" + checked2 + ", species=" + species + ", transcriptsSet=" + transcriptsSet + '}';
    }
}
