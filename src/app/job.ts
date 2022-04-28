import {Thing} from "./thing";
import {Status} from "./status.enum";

export class Job extends Thing {
  public duration : number = 0;
  public requirements : string[] = [];
  public jobStatus : Status = Status.WAITING;
  public progress : number = 0;
}
