package com.sdlctower.domain.deploymentmanagement.policy;

import org.springframework.stereotype.Component;
import java.util.Comparator;

@Component
public class ReleaseVersionComparator implements Comparator<String> {

    @Override
    public int compare(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");
        int len = Math.max(partsA.length, partsB.length);

        for (int i = 0; i < len; i++) {
            String segA = i < partsA.length ? partsA[i] : "";
            String segB = i < partsB.length ? partsB[i] : "";

            boolean numA = segA.matches("\\d+");
            boolean numB = segB.matches("\\d+");

            int cmp;
            if (numA && numB) {
                cmp = Long.compare(Long.parseLong(segA), Long.parseLong(segB));
            } else {
                cmp = segA.compareTo(segB);
            }
            if (cmp != 0) return cmp;
        }
        return 0;
    }
}
