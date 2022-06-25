package tech.makers.aceplay.user;

public class UserDto {
  private Long id;
  private String username;
  private String password;

  protected UserDto() {
  }

  public UserDto(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}