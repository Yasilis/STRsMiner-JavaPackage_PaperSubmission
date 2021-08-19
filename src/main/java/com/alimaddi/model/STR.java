package com.alimaddi.model;

import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.control.converter.STROriginConverter;
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
        name = "STR",
        indexes =
                {@Index(columnList = "sequence", name = "str_sequence_idx"),
                @Index(columnList = "bio_str", name = "str_bio_str_idx"),
                 @Index(columnList = "checked_1", name = "str_checked_1_idx"),
                 @Index(columnList = "checked_2", name = "str_checked_2_idx")})
public class STR implements Serializable
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "origin", nullable = false)
    @Convert(converter = STROriginConverter.class)
    private STROrigin origin;

    @Column(name = "sequence", nullable = false, columnDefinition = "varchar(70)")
    private String sequence;

    @Column(name = "bio_str", nullable = false)
    private boolean bioStr;

    @Column(name = "start_locus", nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    private final Set<Short> startLocus;

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
    @JoinColumn(name = "transcript_stable_id", referencedColumnName = "transcript_stable_id")
    private Transcript transcript;

    public STR()
    {
        startLocus = new HashSet<>();
    }

    public STR(STROrigin origin, String sequence, boolean bioStr, Date updateTime, Transcript transcript)
    {
        this();
        this.origin = origin;
        this.sequence = sequence;
        this.bioStr = bioStr;
        this.lastUpdateTime = updateTime;
        this.transcript = transcript;
    }

    public STR(STROrigin origin, String sequence, boolean bioStr, Date updateTime, Transcript transcript,
               short checked1)
    {
        this(origin, sequence, bioStr, updateTime, transcript);
        setChecked1(checked1);
    }
    public STR(STROrigin origin, String sequence, boolean bioStr, Date updateTime, Transcript transcript,
               short checked1, short checked2)
    {
        this(origin, sequence, bioStr, updateTime, transcript, checked1);
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

    public STROrigin getOrigin()
    {
        return origin;
    }

    public void setOrigin(STROrigin origin)
    {
        this.origin = origin;
    }

    public String getSequence()
    {
        return sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public boolean isBioStr()
    {
        return bioStr;
    }

    public void setBioStr(boolean bioStr)
    {
        this.bioStr = bioStr;
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

    public Transcript getTranscript()
    {
        return transcript;
    }

    public void setTranscript(Transcript transcript)
    {
        this.transcript = transcript;
    }

    public Set<Short> getStartLocus()
    {
        return startLocus;
    }

    public void addStartLocus(Short startLocus)
    {
        this.startLocus.add(startLocus);
    }

    public void addAllStartLoci(Collection<Short> startLoci)
    {
        this.startLocus.addAll(startLoci);
    }

    @Override
    public int hashCode()
    {
//        return Objects.hash(this.name, this.commonName, this.displayName);
        return Objects.hash(transcript.getTranscriptStableID(), sequence, origin);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (this == obj)
            return true;
        STR str = (STR) obj;
        boolean sw = (Objects.equals(sequence, str.sequence) &&
                Objects.equals(bioStr, str.bioStr) &&
                Objects.equals(origin, str.origin) &&
                Objects.equals(transcript, str.transcript));
        return sw;
//        if (!sw)
//            return false;
//
//        for (Short locus : str.startLocus)
//        {
//            if (startLocus.contains(locus))
//                return true;
//        }
//
//        return false;
    }

    @Override
    public String toString()
    {
        return "STR{" + "id=" + id + ", origin=" + origin + ", sequence='" + sequence + '\'' + ", bioStr=" + bioStr + ", startLocus=" + startLocus + ", lastUpdateTime=" + lastUpdateTime + ", checked1=" + checked1 + ", checked2=" + checked2 + ", transcript=" + transcript + '}';
    }
}
