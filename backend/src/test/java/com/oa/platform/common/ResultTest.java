package com.oa.platform.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void successShouldContainPayloadAndSuccessCode() {
        Result<String> result = Result.success("已就绪");

        assertThat(result.code()).isEqualTo(Result.SUCCESS_CODE);
        assertThat(result.message()).isEqualTo("操作成功");
        assertThat(result.data()).isEqualTo("已就绪");
    }
}
