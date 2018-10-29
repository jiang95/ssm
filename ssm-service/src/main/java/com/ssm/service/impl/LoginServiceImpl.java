package com.ssm.service.impl;

import com.ssm.common.constant.Constants;
import com.ssm.common.utils.cache.CacheUtils;
import com.ssm.common.utils.http.ResponseCodeEnum;
import com.ssm.common.utils.http.ResponseModel;
import com.ssm.common.utils.md5.MD5Utils;
import com.ssm.model.DO.UserDO;
import com.ssm.persist.UserMapper;
import com.ssm.service.LoginService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * @author: Lucifer
 * @date: 2018/10/27 17:05
 * @description:
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void sendCode(String phone) {
        Random random = new Random();
        String code = MD5Utils.selectRandom(6);
        CacheUtils.set(Constants.redisLoginVerificationNum, phone, code);
    }

    @Override
    public ResponseModel login(String phone, String code, String password) {

        UserDO user = userMapper.selectByPhone(phone);
        //用户不存在
        if (user == null) {
            return new ResponseModel(ResponseCodeEnum.USER_NOT_EXIST);
        }

        //验证验证码是否正确
        String verificationNum = CacheUtils.get(Constants.redisLoginVerificationNum, phone);
        if (!Objects.equals(verificationNum, code)) {
            return new ResponseModel(ResponseCodeEnum.ILLEGAL_PARAM_AUTHCODE);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.redisUserInfo)
                .append("_")
                .append(phone)
                .append(MD5Utils.selectRandom(6));
        String token = MD5Utils.MD5(builder.toString());
        //设置token
        CacheUtils.set(Constants.redisUserToken, phone, token, Constants.APP_LOGIN_VALID);
        return null;
    }
}
