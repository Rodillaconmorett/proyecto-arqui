package simulation.cache.dataCache.impl;

import simulation.block.dataBlock.DataBlock;
import simulation.directory.directory.Directory;
import simulation.sharedMemory.SharedMemory;

import java.util.concurrent.Semaphore;

public abstract class DataCacheImpl implements simulation.cache.dataCache.DataCache {



    //
    Semaphore memoryBus;
    //
    DataBlock cache;
    //
    SharedMemory sharedMemory;
    //
    Directory directoryLocal;
    //
    Directory directoryRemote;






}
