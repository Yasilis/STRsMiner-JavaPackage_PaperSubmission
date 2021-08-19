package com.alimaddi.datatypes;

public enum  TranscriptSequenceType
{
    EXON_SEQUENCE((short) 0),
    CDNA_SEQUENCE((short)1),
    CODING_SEQUENCE((short)2),
    PEPTIDE_SEQUENCE((short)3);

    private short index;

    TranscriptSequenceType(short index)
    {
        this.index = index;
    }

    public short getIndex()
    {
        return this.index;
    }

    public static TranscriptSequenceType fromCode(short index)
    {
        for (TranscriptSequenceType type : TranscriptSequenceType.values())
        {
            if (type.getIndex() == index)
            {
                return type;
            }
        }
        throw new UnsupportedOperationException("The Index " + index + " is not supported!");
    }
}
