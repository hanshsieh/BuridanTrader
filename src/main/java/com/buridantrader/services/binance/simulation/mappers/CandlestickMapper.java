package com.buridantrader.services.binance.simulation.mappers;

import com.buridantrader.services.binance.CandlestickModel;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Nonnull;
import java.util.List;

public interface CandlestickMapper {
    void insertOrReplace(@Nonnull @Param("candlestick") CandlestickModel candlestick);

    @Nonnull
    List<CandlestickModel> query(
            @Param("startTime") long startTime,
            @Param("endTime") long endTime);

    void createTable();
    void dropTableIfExist();
}
