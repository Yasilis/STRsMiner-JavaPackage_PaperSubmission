package com.alimaddi.control.converter;

import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.datatypes.TranscriptSequenceType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TranscriptSequenceTypeConverter implements AttributeConverter<TranscriptSequenceType, Short>
{
    @Override
    public Short convertToDatabaseColumn(TranscriptSequenceType transcriptSequenceType)
    {
        if (transcriptSequenceType == null)
            return null;

        switch (transcriptSequenceType)
        {
            case EXON_SEQUENCE:
                return 0;
            case CDNA_SEQUENCE:
                return 1;
            case CODING_SEQUENCE:
                return 2;
            case PEPTIDE_SEQUENCE:
                return 3;
            default:
                throw new IllegalArgumentException(transcriptSequenceType + " not supported.");
        }
    }

    @Override
    public TranscriptSequenceType convertToEntityAttribute(Short dbData)
    {
        if (dbData == null)
            return null;

        switch (dbData)
        {
            case 0:
                return TranscriptSequenceType.EXON_SEQUENCE;
            case 1:
                return TranscriptSequenceType.CDNA_SEQUENCE;
            case 2:
                return TranscriptSequenceType.CODING_SEQUENCE;
            case 3:
                return TranscriptSequenceType.PEPTIDE_SEQUENCE;
            default:
                throw new IllegalArgumentException(dbData + " not supported.");
        }
    }
}
