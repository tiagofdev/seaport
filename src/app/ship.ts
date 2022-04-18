import {Thing} from "./thing";
import {Job} from "./job";

export class Ship extends Thing {
  public dock : string = "";
  public port : string = "";
  public jobs : Job[] = [];
}
