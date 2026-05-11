public abstract class Tile{
  private final String name;
  private final int position;

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
  public abstract void landOn(Player player); //when the player lands on that particular tile

  @Override
  public String toString(){
    return name + "(position " + position + ")";
  }
}
