package com.alimaddi.datatypes;

public enum STROrigin
{
    CDS_START_CODON((short) 0),
    CDS_EXCEPT_START_CODON((short)1),
    CDNA_START_CODON((short)2),
    CDNA_EXCEPT_START_CODON((short)3);

    private final short index;

    STROrigin(short index)
    {
        this.index = index;
    }

    public short getIndex()
    {
        return this.index;
    }

    public static STROrigin fromCode(short index)
    {
        for (STROrigin status : STROrigin.values())
        {
            if (status.getIndex() == index)
            {
                return status;
            }
        }
        throw new UnsupportedOperationException("The Index " + index + " is not supported!");
    }
}
