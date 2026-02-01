package com.devinsight.service;

import com.devinsight.entity.User;
import com.devinsight.exception.BusinessException;
import com.devinsight.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 清除敏感信息
        user.setPassword(null);
        return user;
    }
}
