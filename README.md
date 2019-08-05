# BuridanTrader
[![Build Status](https://travis-ci.com/sleepingpig/BuridanTrader.svg?branch=master)](https://travis-ci.com/sleepingpig/BuridanTrader)

An automatic virtual currency trading bot.
It is still under development, and isn't ready to be used.

# Simulation
To allow simulation of the program before running with real assets, a simulator is written.  
Currently, th simulator can only download the history data.  
It is not yet able to run the program with the simulation data.  
To collection the data, run the jar with:
```
java -cp <jar with dependencies> com.buridantrader.services.binance.simulation.SimulationDataCollector <config path> 
``` 
There's a sample config file at class path `com/buridantrader/services/binance/simulation/config_sample.yaml`.  
