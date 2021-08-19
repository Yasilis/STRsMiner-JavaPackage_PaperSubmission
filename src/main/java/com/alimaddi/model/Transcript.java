package com.alimaddi.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author Ali Maddi
 */
//TODO : change annotation to the org.hibernate.annotations!
@Entity
@Table(
        name = "Transcript",
        indexes =
                {@Index(columnList = "transcript_stable_id", name = "transcript_transcript_stable_id_idx"),
                 @Index(columnList = "checked_1", name = "transcript_checked_1_idx"),
                 @Index(columnList = "checked_2", name = "transcript_checked_2_idx")},
        uniqueConstraints =
                {@UniqueConstraint(columnNames = "transcript_stable_id", name = "transcript_transcript_stable_id_unq")})
public class Transcript implements Serializable
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "transcript_stable_id", nullable = false, columnDefinition = "varchar(25)")
    private String transcriptStableID;

    @Column(name = "transcript_type", nullable = false, columnDefinition = "varchar(25)")
    private String type;

    @Column(name = "nucleotide_sequence", columnDefinition = "varchar(150)")
    @Type(type = "text")
    private String nucleotideSequence;

    @Column(name = "peptide_sequence", columnDefinition = "varchar(50)")
    @Type(type = "text")
    private String peptideSequence;

    @Column(name = "cds", columnDefinition = "varchar(150)")
    @Type(type = "text")
    private String cds;

    @Column(name = "cds_promoter", columnDefinition = "varchar(150)")
    private String cdsPromoter;

    @Column(name = "cdna", columnDefinition = "varchar(150)")
    @Type(type = "text")
    private String cdna;

    @Column(name = "cdna_promoter", columnDefinition = "varchar(150)")
    private String cdnaPromoter;

    @Column(name = "last_update_time", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastUpdateTime;

    @Column(name = "checked_1", nullable = false)
    @ColumnDefault("0")
    private short checked1;

    @Column(name = "checked_2", nullable = false)
    @ColumnDefault("0")
    private short checked2;

//    @Column(name = "gene_stable_id", nullable = false, columnDefinition = "varchar(25)")
//    private String geneStableID;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "gene_stable_id", referencedColumnName = "gene_stable_id")
    private Gene gene;

    @OneToMany(mappedBy = "transcript", cascade = CascadeType.PERSIST)
    private final Set<STR> strsSet;

    public Transcript()
    {
        strsSet = new HashSet<>();
    }

    public Transcript(String transcriptStableID, String type, String nucleotideSequence, String peptideSequence,
                      String cds, String cdsPromoter, String cdna, String cdnaPromoter, Date updateTime, Gene gene)
    {
        this();
        this.transcriptStableID = transcriptStableID;
        this.type = type;
        this.nucleotideSequence = nucleotideSequence;
        this.peptideSequence = peptideSequence;
        this.cds = cds;
        this.cdsPromoter = cdsPromoter;
        this.cdna = cdna;
        this.cdnaPromoter = cdnaPromoter;
        this.lastUpdateTime = updateTime;
        this.gene = gene;
    }

    public Transcript(String transcriptStableID, String type, String nucleotideSequence, String peptideSequence,
                      String cds, String cdsPromoter, String cdna, String cdnaPromoter, Date updateTime, Gene gene,
                      short checked1)
    {
        this(transcriptStableID, type, nucleotideSequence, peptideSequence, cds, cdsPromoter,
             cdna, cdnaPromoter, updateTime, gene);
        setChecked1(checked1);
    }

    public Transcript(String transcriptStableID, String type, String nucleotideSequence, String peptideSequence,
                      String cds, String cdsPromoter, String cdna, String cdnaPromoter, Date updateTime, Gene gene,
                      short checked1, short checked2)
    {
        this(transcriptStableID, type, nucleotideSequence, peptideSequence, cds, cdsPromoter,
             cdna, cdnaPromoter, updateTime, gene, checked1);
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

    public String getTranscriptStableID()
    {
        return transcriptStableID;
    }

    public void setTranscriptStableID(String stableID)
    {
        this.transcriptStableID = stableID;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getNucleotideSequence()
    {
        return nucleotideSequence;
    }

    public void setNucleotideSequence(String nucleotideSequence)
    {
        this.nucleotideSequence = nucleotideSequence;
    }

    public String getPeptideSequence()
    {
        return peptideSequence;
    }

    public void setPeptideSequence(String peptideSequence)
    {
        this.peptideSequence = peptideSequence;
    }

    public String getCds()
    {
        return cds;
    }

    public void setCds(String cds)
    {
        this.cds = cds;
    }

    public String getCdsPromoter()
    {
        return cdsPromoter;
    }

    public void setCdsPromoter(String cdsPromoter)
    {
        this.cdsPromoter = cdsPromoter;
    }

    public String getCdna()
    {
        return cdna;
    }

    public void setCdna(String cdna)
    {
        this.cdna = cdna;
    }

    public String getCdnaPromoter()
    {
        return cdnaPromoter;
    }

    public void setCdnaPromoter(String cdnaPromoter)
    {
        this.cdnaPromoter = cdnaPromoter;
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

    public Gene getGene()
    {
        return gene;
    }

    public void setGene(Gene gene)
    {
        this.gene = gene;
    }

    public Set<STR> getStrsSet()
    {
        return strsSet;
    }

    public void addSTR(STR str)
    {
        this.strsSet.add(str);
    }

    public void addAllSTRs(Collection<STR> strs)
    {
        this.strsSet.addAll(strs);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.transcriptStableID, this.gene.getGeneStableID(), this.type);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (this == obj)
            return true;

        Transcript transcript = (Transcript) obj;
        return Objects.equals(transcriptStableID, transcript.transcriptStableID) &&
                Objects.equals(type, transcript.type) &&
                Objects.equals(gene, transcript.gene);// &&
//                Objects.equals(sequence, transcript.sequence) &&
//                Objects.equals(cds, transcript.cds) &&
//                Objects.equals(cdna, transcript.cdna);
    }

    @Override
    public String toString()
    {
        return "Transcript{" + "id=" + id + ", transcriptStableID='" + transcriptStableID + '\'' + ", type='" + type + '\'' + ", nucleotideSequence='" + nucleotideSequence + '\'' + ", peptideSequence='" + peptideSequence + '\'' + ", cds='" + cds + '\'' + ", cdsPromoter='" + cdsPromoter + '\'' + ", cdna='" + cdna + '\'' + ", cdnaPromoter='" + cdnaPromoter + '\'' + ", lastUpdateTime=" + lastUpdateTime + ", checked1=" + checked1 + ", checked2=" + checked2 + ", gene=" + gene + ", strsSet=" + strsSet + '}';
    }
}
