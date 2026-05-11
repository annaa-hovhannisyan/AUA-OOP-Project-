public abstract class Tile{
  private String name;
  private int position;

  public Tile(String name, int position){
    this.name = name;
    this.position = position;
  }
  public String getName(){
    return name;
  }
  public int getPosition(){
    return position;
  }
  public abstract void landOn(Player player); //when the player lnads on that particular tile

  @Override
  public String toString(){
    return name + "(position " + position + ")";
  }
}
