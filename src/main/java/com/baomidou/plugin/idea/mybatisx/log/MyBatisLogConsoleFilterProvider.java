package com.baomidou.plugin.idea.mybatisx.log;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * MyBatisLogConsoleFilterProvider
 *
 * @author huangxingguang
 */
public class MyBatisLogConsoleFilterProvider implements ConsoleFilterProvider {
    private final Key<MyBatisLogConsoleFilter> KEY = Key.create(MyBatisLogConsoleFilter.class.getName());

    public MyBatisLogConsoleFilterProvider() {

    }

    @Override
    public Filter[] getDefaultFilters(@NotNull Project project) {
        UserDataHolder holder = (UserDataHolder) project;

        MyBatisLogConsoleFilter filter = holder.getUserData(KEY);
        if (Objects.isNull(filter)) {
            filter = new MyBatisLogConsoleFilter(project);
            holder.putUserData(KEY, filter);
        }
        return new Filter[] { filter };
    }
}
