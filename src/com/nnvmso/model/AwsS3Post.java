package com.nnvmso.model;

/*
 * necessary inputs when posting data to aws s3
 */
public class AwsS3Post {

	private String bucket_name; 
	private String upload_url;
	private String AWSAccessKeyId;
	private String key;
	private String acl;    		
	private String content_type;
	private String success_action_redirect;
	private String signature;   		
	private String policy;
	private String filename;
	private String x_amz_meta_filename; //fullname
	private String x_amz_meta_token;
	private String x_amz_meta_creatDate;   
	private long pid;
	
	public AwsS3Post() {}

	public String getUpload_url() {
		return upload_url;
	}

	public void setUpload_url(String upload_url) {
		this.upload_url = upload_url;
	}

	public String getAWSAccessKeyId() {
		return AWSAccessKeyId;
	}

	public void setAWSAccessKeyId(String aWSAccessKeyId) {
		AWSAccessKeyId = aWSAccessKeyId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAcl() {
		return acl;
	}

	public void setAcl(String acl) {
		this.acl = acl;
	}

	public String getSuccess_action_redirect() {
		return success_action_redirect;
	}

	public void setSuccess_action_redirect(String success_action_redirect) {
		this.success_action_redirect = success_action_redirect;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public String getX_amz_meta_filename() {
		return x_amz_meta_filename;
	}

	public void setX_amz_meta_filename(String x_amz_meta_filename) {
		this.x_amz_meta_filename = x_amz_meta_filename;
	}

	public String getX_amz_meta_token() {
		return x_amz_meta_token;
	}

	public void setX_amz_meta_token(String x_amz_meta_token) {
		this.x_amz_meta_token = x_amz_meta_token;
	}

	public String getX_amz_meta_creatDate() {
		return x_amz_meta_creatDate;
	}

	public void setX_amz_meta_creatDate(String x_amz_meta_creatDate) {
		this.x_amz_meta_creatDate = x_amz_meta_creatDate;
	}

	public String getBucket_name() {
		return bucket_name;
	}

	public void setBucket_name(String bucket_name) {
		this.bucket_name = bucket_name;
	}

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

}
