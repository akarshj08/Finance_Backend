package com.finance.util;

import java.time.LocalDate;
import java.time.YearMonth;

public final class DateRangeUtil
{

    private DateRangeUtil() {}

    public static LocalDate startOfMonth(int year,int month)
    {
        return YearMonth.of(year, month).atDay(1);
    }

    public static LocalDate endOfMonth(int year, int month)
    {
        return YearMonth.of(year, month).atEndOfMonth();
    }

    public static LocalDate monthsAgo(int months)
    {
        return LocalDate.now().minusMonths(months).withDayOfMonth(1);
    }

    public static LocalDate startOfCurrentYear()
    {
        return LocalDate.now().withDayOfYear(1);
    }

    public static LocalDate today()
    {
        return LocalDate.now();
    }
}
