package com.alimaddi.control.converter;

import com.alimaddi.datatypes.STROrigin;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class STROriginConverter implements AttributeConverter<STROrigin, Short>
{
    @Override
    public Short convertToDatabaseColumn(STROrigin strOrigin)
    {
        if (strOrigin == null)
            return null;

        switch (strOrigin)
        {
            case CDS_START_CODON:
                return 0;
            case CDS_EXCEPT_START_CODON:
                return 1;
            case CDNA_START_CODON:
                return 2;
            case CDNA_EXCEPT_START_CODON:
                return 3;
            default:
                throw new IllegalArgumentException(strOrigin + " not supported.");
        }
    }

    @Override
    public STROrigin convertToEntityAttribute(Short dbData)
    {
        if (dbData == null)
            return null;

        switch (dbData)
        {
            case 0:
                return STROrigin.CDS_START_CODON;
            case 1:
                return STROrigin.CDS_EXCEPT_START_CODON;
            case 2:
                return STROrigin.CDNA_START_CODON;
            case 3:
                return STROrigin.CDNA_EXCEPT_START_CODON;
            default:
                throw new IllegalArgumentException(dbData + " not supported.");
        }
    }
}
