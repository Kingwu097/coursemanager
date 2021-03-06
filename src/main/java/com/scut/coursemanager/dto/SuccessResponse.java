package com.scut.coursemanager.dto;/*

 */

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuccessResponse {
    @ApiModelProperty(value = "状态码", name = "status", example = "200")
    private Short status = 200;

    @ApiModelProperty(value = "请求是否成功", name = "success", example = "true")
    private Boolean success;

    @ApiModelProperty(value = "描述信息", name = "message", example = "请求成功")
    private String message;

    public SuccessResponse(Boolean success,String message){
        this.message=message;
        this.success=success;
    }
}
