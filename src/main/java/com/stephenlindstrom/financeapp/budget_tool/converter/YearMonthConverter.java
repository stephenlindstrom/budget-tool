package com.stephenlindstrom.financeapp.budget_tool.converter;

import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

  @Override
  public String convertToDatabaseColumn(YearMonth attribute) {
    return attribute != null ? attribute.toString() : null;
  }

  @Override
  public YearMonth convertToEntityAttribute(String dbData) {
    return dbData != null ? YearMonth.parse(dbData) : null;
  }
}
