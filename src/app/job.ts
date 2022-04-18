import {Thing} from "./thing";

export class Job extends Thing {
  public duration : number = 0;
  public requirements : string[] = [];
}
