package org.freedom.moniter.bean;
public class Endport {
	private String endportId;
    private String endportDesc;
    private Long connId;
  
    public Long getConnId() {
		return connId;
	}

	public void setConnId(Long connId) {
		this.connId = connId;
	}

	public String getEndportId() {
		return endportId;
	}

	public void setEndportId(String endportId) {
		this.endportId = endportId;
	}

	public String getEndportDesc() {
		return endportDesc;
	}

	public void setEndportDesc(String endportDesc) {
		this.endportDesc = endportDesc;
	}

	@Override
    public String toString(){
        return getEndportId() + ", "+getEndportDesc() + "," + getConnId();
    }

}
