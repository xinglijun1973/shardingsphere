/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.transaction.cases.readonly;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.junit.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL set read only transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.POSTGRESQL})
@Slf4j
public final class PostgreSQLSetReadOnlyTestCase extends SetReadOnlyTestCase {
    
    public PostgreSQLSetReadOnlyTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertSetReadOnly();
        assertNotSetReadOnly();
    }
    
    private void assertSetReadOnly() throws SQLException {
        Connection connection1 = getDataSource().getConnection();
        executeUpdateWithLog(connection1, "insert into account(id, balance) values (1, 0), (2, 100);");
        Connection connection2 = getDataSource().getConnection();
        connection2.setReadOnly(true);
        assertQueryBalance(connection2);
        try {
            executeWithLog(connection2, "update account set balance = 100 where id = 2;");
            log.info("Using the driver of postgresql:42.4.1 expect to update successfully.");
        } catch (SQLException e) {
            Assert.fail("Update failed, should be successfully.");
        }
    }
}
