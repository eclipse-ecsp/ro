/*
 *
 * ******************************************************************************
 *
 *  Copyright (c) 2023-24 Harman International
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 **
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 *
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  *******************************************************************************
 *
 */

package org.eclipse.ecsp.ro.utils;

import org.eclipse.ecsp.ro.constants.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

/**
 * test class for  StockingRuleConfigUtil.
 */
public class StockingRuleConfigUtilTest {

    @Test
    public void getStockingRuleConfig() {
        Map<String, Object> input = new HashMap();
        Map<String, String> object = new HashMap<>();
        object.put("mappingName", "vehicle_archType1_precondition");
        input.put(Constants.STOCKING_RULE_CONFIGURATIONOBJECT, object);
        Map<String, String> resultMap = StockingRuleConfigUtil.getStockingRuleConfig(input);
        Assertions.assertEquals("vehicle_archType1_precondition", resultMap.get("mappingName"));
    }
}