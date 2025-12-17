package com.ks.tocho5.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchUpdateResponse {
    public List<Integer> ok = new ArrayList<>();
    public Map<Integer, String> errors = new HashMap<>();
}
