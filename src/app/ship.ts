import {Thing} from "./thing";
import {Job} from "./job";
import {Status} from "./status.enum";

export class Ship extends Thing {
  public dock : string = "";
  public port : string = "";
  public jobs : Job[] = [];
  public shipStatus : Status = Status.DOCKING;
}
