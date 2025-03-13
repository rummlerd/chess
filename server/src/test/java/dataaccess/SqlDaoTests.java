package dataaccess;

public class SqlDaoTests extends AbstractDaoTests {
    @Override
    protected DataAccess getDataAccess() {
        return new SqlDataAccess();
    }
}
