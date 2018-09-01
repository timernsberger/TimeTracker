package com.times6.timeTracker.db.dynamo;

import com.times6.timeTracker.TaskType;
import com.times6.timeTracker.db.dynamo.TaskTypeRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TaskTypeRecordTest {
    @Test
    public void toTaskTypeCopiesAllFields() throws Exception {
        TaskTypeRecord record = new TaskTypeRecord();
        record.setCategory("Test");
        record.setName("copy");
        record.setUserId("tester");
        record.setRangeKey("Test|copy");

        TaskType converted = record.toTaskType();

        assertThat(converted.getCategory(), equalTo(record.getCategory()));
        assertThat(converted.getName(), equalTo(record.getName()));

       assertGettersNonNull(converted);
    }

    @Test
    public void fromTaskTypeCopiesAllFields() throws Exception {
        TaskType data = new TaskType();
        data.setCategory("test");
        data.setName("convert");
        String userId = "test account";

        TaskTypeRecord record = TaskTypeRecord.fromTaskType(data, userId);

        assertThat(record.getCategory(), equalTo(data.getCategory()));
        assertThat(record.getName(), equalTo(data.getName()));
        assertThat(record.getUserId(), equalTo(userId));

        // key uniqueness check
        assertThat(record.getRangeKey(), containsString(data.getCategory()));
        assertThat(record.getRangeKey(), containsString(data.getName()));

        assertGettersNonNull(record);
    }

    /*
     * If something goes wrong with this class, it will probably be due to adding a field
     * and forgetting to add it to the conversion methods. Writing out assertions for each
     * field won't catch that either if we also forget to add the field to the tests. This
     * method blindly checks all fields (all fields we care about should have getters) to
     * see if they're populated. This should fail the test if we add any non-primitive
     * field and don't make the conversion methods copy tbe value into the new object.
     */
    private void assertGettersNonNull(Object object) throws Exception {
        for(Method method : object.getClass().getDeclaredMethods()) {
            if(method.getName().startsWith("get")) {
                assertThat(method.invoke(object), notNullValue());
            }
        }
    }
}
