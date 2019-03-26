package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UcenterService {

    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username) {
        //1.根据账号查询XcUser信息
        XcUser xcUser = xcUserRepository.findXcUserByUsername(username);
        if (xcUser == null){
            return null;
        }
        //2.根据用户id查询所属企业id
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(xcUser.getId());
        String companyId = null;
        if (xcCompanyUser != null){
            companyId = xcCompanyUser.getCompanyId();
        }
        //3.根据用户id查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if (xcMenus == null){
            return null;
        }
        //4.给XcUserExt赋值
        XcUserExt xcUserExt = new XcUserExt();
        //将xcUser信息拷贝到xcUserExt
        BeanUtils.copyProperties(xcUser, xcUserExt);
        xcUserExt.setCompanyId(companyId);
        xcUserExt.setPermissions(xcMenus);
        return xcUserExt;
    }
}
