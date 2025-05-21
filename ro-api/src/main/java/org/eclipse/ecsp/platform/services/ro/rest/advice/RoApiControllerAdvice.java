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

package org.eclipse.ecsp.platform.services.ro.rest.advice;

import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.platform.services.ro.domain.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RO API Controller Advice Class.
 * All advices for RO APIs Controllers will be handled here.
 */
@RestControllerAdvice
public class RoApiControllerAdvice {

    /**
     * Exception Handler Method for BadRequest Exception.
     */
    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<ApiResponse<String>> handleException(BadRequestException ex) {
        ApiResponse<String> resp = new ApiResponse<>();
        resp.setMessage(ex.getMessage());
        resp.setStatusCode(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }
}
