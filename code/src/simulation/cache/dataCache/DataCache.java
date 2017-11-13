package simulation.cache.dataCache;

import simulation.block.dataBlock.DataBlock;

public interface DataCache {

    /**
     *
     * @param index
     * @return data
     */
    public int readData(int index);

    /**
     *
     * @return
     */
    DataBlock findMemory();

    /**
     *
     * @return
     */
    int findDirectory();


}
