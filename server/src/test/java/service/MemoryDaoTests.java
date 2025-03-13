package service;

import dataaccess.AbstractDaoTests;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

public class MemoryDaoTests extends AbstractDaoTests {
    @Override
    protected DataAccess getDataAccess() {
        return new MemoryDataAccess();
    }

    @Override
    protected boolean memoryDaoTesting() {
        return true;
    }
}
