package com.jc.integralgradle.controller;

import com.jc.integralgradle.client.AssetClient;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequestMapping("/account")
public class TestController {

    @Autowired
    private AssetClient assetClient;

    /**
     * 传入账号获取积分信息
     *
     * @param amount 账号
     * @return 积分
     */
    @GetMapping
    public String getAccount(String amount) {
        Tuple2<BigInteger, BigInteger> assetAmount = assetClient.queryAssetAmount(amount);
        return assetAmount == null ? "暂无" : assetAmount.getValue2().toString();
    }

    @PostMapping
    public String insertAccount(String amount, Integer integral) {
        BigInteger param = new BigInteger(integral.toString());
        int i = assetClient.registerAssetAccount(amount, param);
        return i == 1 ? "success" : "fail";
    }

    @PutMapping
    public String updateAccount(String amount, Integer integral, Integer isReduce) {
        Tuple2<BigInteger, BigInteger> tuple2 = assetClient.queryAssetAmount(amount);
        BigInteger value2 = tuple2.getValue2();
        BigInteger subtract = value2.subtract(new BigInteger(integral.toString()));
        int i = 0;
        if (subtract.compareTo(new BigInteger("0")) >= 0) {
            BigInteger param = new BigInteger(integral.toString());
            i = assetClient.updateAssetAccount(amount, param, new BigInteger(isReduce.toString()));
        }
        return i == 1 ? "success" : "fail";
    }

    @DeleteMapping
    public String deleteAccount(String amount) {
        int i = assetClient.deleteAssetAccount(amount);
        return i == 1 ? "success" : "fail";
    }
}
