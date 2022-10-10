package net.verytools.unipay.utils;

import com.alipay.api.AlipayResponse;
import net.verytools.unipay.api.BaseApiResult;

public class CodeMsgExtractor {

    public static void extract4Alipay(AlipayResponse alipayResponse, BaseApiResult apiResult) {
        apiResult.setCode(alipayResponse.getCode());
        apiResult.setMsg(alipayResponse.getMsg());
        apiResult.setSubCode(alipayResponse.getSubCode());
        apiResult.setSubMsg(alipayResponse.getSubMsg());
    }

}
