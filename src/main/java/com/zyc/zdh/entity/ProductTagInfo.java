package com.zyc.zdh.entity;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Table(name = "product_tag_info")
public class ProductTagInfo {
    @Id
    private String id;

    /**
     * 产品标识code
     */
    private String product_code;

    /**
     * 产品名称
     */
    private String product_name;

    /**
     * 拥有者
     */
    private String owner;

    /**
     * 是否删除,0:未删除,1:删除
     */
    private String is_delete;

    /**
     * 创建时间
     */
    private Timestamp create_time;

    /**
     * 更新时间
     */
    private Timestamp update_time;

    /**
     * 0:启用,1:不可申请,2:禁用
     */
    private String status;

    /**
     * 产品ak
     */
    private String ak;

    /**
     * 产品sk
     */
    private String sk;

    /**
     * 产品类型
     */
    private String product_type;


    /**
     * 产品管理员账号
     */
    private String product_admin;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取产品标识code
     *
     * @return product_code - 产品标识code
     */
    public String getProduct_code() {
        return product_code;
    }

    /**
     * 设置产品标识code
     *
     * @param product_code 产品标识code
     */
    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    /**
     * 获取产品名称
     *
     * @return product_name - 产品名称
     */
    public String getProduct_name() {
        return product_name;
    }

    /**
     * 设置产品名称
     *
     * @param product_name 产品名称
     */
    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    /**
     * 获取拥有者
     *
     * @return owner - 拥有者
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 设置拥有者
     *
     * @param owner 拥有者
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 获取是否删除,0:未删除,1:删除
     *
     * @return is_delete - 是否删除,0:未删除,1:删除
     */
    public String getIs_delete() {
        return is_delete;
    }

    /**
     * 设置是否删除,0:未删除,1:删除
     *
     * @param is_delete 是否删除,0:未删除,1:删除
     */
    public void setIs_delete(String is_delete) {
        this.is_delete = is_delete;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Timestamp getCreate_time() {
        return create_time;
    }

    /**
     * 设置创建时间
     *
     * @param create_time 创建时间
     */
    public void setCreate_time(Timestamp create_time) {
        this.create_time = create_time;
    }

    /**
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Timestamp getUpdate_time() {
        return update_time;
    }

    /**
     * 设置更新时间
     *
     * @param update_time 更新时间
     */
    public void setUpdate_time(Timestamp update_time) {
        this.update_time = update_time;
    }

    /**
     * 获取0:启用,1:不可申请,2:禁用
     *
     * @return status - 0:启用,1:不可申请,2:禁用
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置0:启用,1:不可申请,2:禁用
     *
     * @param status 0:启用,1:不可申请,2:禁用
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取产品ak
     *
     * @return ak - 产品ak
     */
    public String getAk() {
        return ak;
    }

    /**
     * 设置产品ak
     *
     * @param ak 产品ak
     */
    public void setAk(String ak) {
        this.ak = ak;
    }

    /**
     * 获取产品sk
     *
     * @return sk - 产品sk
     */
    public String getSk() {
        return sk;
    }

    /**
     * 设置产品sk
     *
     * @param sk 产品sk
     */
    public void setSk(String sk) {
        this.sk = sk;
    }

    /**
     * 获取产品类型
     *
     * @return product_type - 产品类型
     */
    public String getProduct_type() {
        return product_type;
    }

    /**
     * 设置产品类型
     *
     * @param product_type 产品类型
     */
    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getProduct_admin() {
        return product_admin;
    }

    public void setProduct_admin(String product_admin) {
        this.product_admin = product_admin;
    }
}