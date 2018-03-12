package com.workflow.utils;

import java.util.Collection;

public class CollectionUtils {
    public static boolean isEmpty(@SuppressWarnings("rawtypes") Collection collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(@SuppressWarnings("rawtypes") Collection collection) {
        return !CollectionUtils.isEmpty(collection);
    }

}
