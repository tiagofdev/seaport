package titi.seaport;

public class Person extends Thing {
    private String skill;


    public void setSkill(String s) {
        skill = s;
    }
    public String getSkill() {
        return skill;
    }

    public void setAdditionalInformation() {
        this.info = "Skill: " + skill;
    }

}
