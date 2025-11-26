package com.hwn.sw_project.service.match;

import com.hwn.sw_project.dto.gov24.SupportConditionsDTO;
import com.hwn.sw_project.dto.gov24.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class SupportMatcher {

    private static boolean Y(String v){
        if (v == null) return false;
        String t = v.trim();
        return "Y".equalsIgnoreCase(t) || "예".equals(t) || "true".equalsIgnoreCase(t) || "1".equals(t);
    }

    public static boolean matchGender(SupportConditionsDTO s, String g){
        boolean any = Y(s.JA0101()) || Y(s.JA0102());
        if(!any) return true;
        if (g == null) return true;
        return ("M".equals(g) && Y(s.JA0101())) || ("F".equals(g) && Y(s.JA0102()));
    }

    public static boolean matchAge(SupportConditionsDTO s, Integer age){
        if(age == null) return true;
        Integer from = s.JA0110(), to = s.JA0111();
        if(from==null && to==null) return true;
        if(from!=null && age<from) return false;
        if(to!=null && age>to) return false;
        return true;
    }

    public static boolean matchIncome(SupportConditionsDTO s, String b){
        boolean any = Y(s.JA0201())||Y(s.JA0202())||Y(s.JA0203())||Y(s.JA0204())||Y(s.JA0205());
        if(!any) return true;
        if(b==null||"모름".equals(b)) return true;
        return switch (b){
            case "0-50" -> Y(s.JA0201());
            case "51-75" -> Y(s.JA0202());
            case "76-100" -> Y(s.JA0203());
            case "101-200" -> Y(s.JA0204());
            case "200+" -> Y(s.JA0205());
            default -> false;
        };
    }

    public static boolean matchStudent(SupportConditionsDTO s, String st){
        boolean any = Y(s.JA0317())||Y(s.JA0318())||Y(s.JA0319())||Y(s.JA0320());
        if(!any || st==null || "해당사항없음".equals(st)) return true;
        return switch (st){
            case "초등학생" -> Y(s.JA0317());
            case "중학생" -> Y(s.JA0318());
            case "고등학생" -> Y(s.JA0319());
            case "대학생/대학원생" -> Y(s.JA0320());
            default -> true;
        };
    }

    public static boolean matchEmployment(SupportConditionsDTO s, String emp){
        boolean any = Y(s.JA0326())||Y(s.JA0327());
        if(!any || emp==null||"해당사항없음".equals(emp)) return true;
        return ("근로자/직장인".equals(emp) && Y(s.JA0326()))
                || ("구직자/실업자".equals(emp) && Y(s.JA0327()));
    }

    public static boolean matchSpecial(SupportConditionsDTO s, List<String> flags){
        if(flags==null || flags.isEmpty() || flags.contains("해당사항없음")) return true;

        boolean anyTarget =
                Y(s.JA0301())||Y(s.JA0302())||Y(s.JA0303())||
                        Y(s.JA0328())||Y(s.JA0329())||Y(s.JA0330())||
                        Y(s.JA0401())||Y(s.JA0402())||Y(s.JA0403())||Y(s.JA0404())||Y(s.JA0411());
        if(!anyTarget) return true;

        for(String f: flags){
            if( ("예비부모/난임".equals(f) && Y(s.JA0301())) ||
                    ("임산부".equals(f) && Y(s.JA0302())) ||
                    ("출산/입양".equals(f) && Y(s.JA0303())) ||
                    ("장애인".equals(f) && Y(s.JA0328())) ||
                    ("국가보훈대상자".equals(f) && Y(s.JA0329())) ||
                    ("질병/질환자".equals(f) && Y(s.JA0330())) ||
                    ("다문화가족".equals(f) && Y(s.JA0401())) ||
                    ("북한이탈주민".equals(f) && Y(s.JA0402())) ||
                    ("한부모가정/조손가정".equals(f) && Y(s.JA0403())) ||
                    ("1인가구".equals(f) && Y(s.JA0404())) ||
                    ("다자녀가구".equals(f) && Y(s.JA0411()))
            ) return true;
        }
        return false;
    }

    public static boolean matchIndustry(SupportConditionsDTO s, String ind){
        if(ind==null||"해당사항없음".equals(ind)) return true;
        boolean any =
                Y(s.JA1201())||Y(s.JA1202())||Y(s.JA1299())||
                        Y(s.JA2201())||Y(s.JA2202())||Y(s.JA2203())||Y(s.JA2299());
        if(!any) return true;

        return switch (ind){
            case "음식점업" -> Y(s.JA1201());
            case "제조업" -> Y(s.JA1202()) || Y(s.JA2201());
            case "농업/임업/어업" -> Y(s.JA2202());
            case "정보통신업" -> Y(s.JA2203());
            case "기타업종" -> Y(s.JA1299()) || Y(s.JA2299());
            default -> true;
        };
    }

    /* -------------------- 최종 매칭 -------------------- */
    public static boolean matches(SupportConditionsDTO s, UserProfile u){
        return matchGender(s, u.gender())
                && matchAge(s, u.age())
                && matchIncome(s, u.incomeBracket())
                && matchStudent(s, u.studentStatus())
                && matchEmployment(s, u.employmentStatus())
                && matchSpecial(s, u.specialFlags())
                && matchIndustry(s, u.industry());
    }

    /* -------------------- 점수 계산 -------------------- */
    public static double score(SupportConditionsDTO s, UserProfile u){
        double sc=0.0;
        // 성별: +1
        boolean hasGenderCond = Y(s.JA0101()) || Y(s.JA0102());
        if (hasGenderCond && u.gender()!=null && matchGender(s, u.gender())) {
            sc += 1.0;
        }

        // 나이: +[0~1]
        if (u.age()!=null && (s.JA0110()!=null || s.JA0111()!=null)) {
            int from = s.JA0110()==null? u.age(): s.JA0110();
            int to   = s.JA0111()==null? u.age(): s.JA0111();
            if (to < from) { int t=from; from=to; to=t; }
            double mid=(from+to)/2.0;
            double denom = Math.max(1.0, (to - from + 1) / 2.0);
            double ageScore = 1.0 - Math.min(1.0, Math.abs(u.age() - mid) / denom);
            sc+=ageScore;
        }

        // 소득: +1
        boolean hasIncomeCond =
                Y(s.JA0201()) || Y(s.JA0202()) || Y(s.JA0203()) || Y(s.JA0204()) || Y(s.JA0205());
        if (hasIncomeCond
                && u.incomeBracket()!=null
                && !"모름".equals(u.incomeBracket())
                && matchIncome(s, u.incomeBracket())) {
            sc += 1.0;
        }

        // 학생: +1
        boolean hasStudentCond =
                Y(s.JA0317()) || Y(s.JA0318()) || Y(s.JA0319()) || Y(s.JA0320());
        if (hasStudentCond
                && u.studentStatus()!=null
                && !"해당사항없음".equals(u.studentStatus())
                && matchStudent(s, u.studentStatus())) {
            sc += 1;
        }

        // 취업 상태: +1.5
        boolean hasEmpCond = Y(s.JA0326())||Y(s.JA0327());
        if(hasEmpCond
                && u.employmentStatus()!=null
                && !"해당사항없음".equals(u.employmentStatus())
                && matchEmployment(s, u.employmentStatus())) {
            sc+=1.5;
        }

        // 특이사항: +2
        boolean hasSpecialCond =
                        Y(s.JA0301())||Y(s.JA0302())||Y(s.JA0303()) ||
                        Y(s.JA0328())||Y(s.JA0329())||Y(s.JA0330())||
                        Y(s.JA0401())||Y(s.JA0402())||Y(s.JA0403())||Y(s.JA0404())||Y(s.JA0411());
        if(hasSpecialCond
                && u.specialFlags()!=null
                && !u.specialFlags().isEmpty()
                && !u.specialFlags().contains("해당사항없음")
                && matchSpecial(s,u.specialFlags())) {
            sc+=2;
        }

        // 업종: +1
        boolean hasIndustryCond =
                Y(s.JA1201())||Y(s.JA1202())||Y(s.JA1299())||
                Y(s.JA2201())||Y(s.JA2202())||Y(s.JA2203())||Y(s.JA2299());
        if(hasIndustryCond
                && u.industry()!=null
                && !"해당사항없음".equals(u.industry())
                && matchIndustry(s, u.industry())) {
            sc+=1.0;
        }

        return sc;
    }

    /* -------------------- match reason -------------------- */
    public static List<String> reasons(SupportConditionsDTO s, UserProfile u){
        List<String> r = new ArrayList<>();

        if (u.gender()!=null && (Y(s.JA0101())||Y(s.JA0102())) && matchGender(s,u.gender())) {
            r.add("성별: " + ("M".equals(u.gender()) ? "남성" : "여성"));
        }
        if (u.age()!=null && matchAge(s,u.age()) && (s.JA0110()!=null || s.JA0111()!=null)) {
            r.add("나이: " + u.age() + "세"
                    + " (범위 " + (s.JA0110()==null?"-":s.JA0110()) + "~" + (s.JA0111()==null?"-":s.JA0111()) + ")");
        }
        if (u.incomeBracket()!=null && matchIncome(s,u.incomeBracket())) {
            r.add("소득: " + switch (u.incomeBracket()) {
                case "0-50" -> "0~50%";
                case "51-75" -> "51~75%";
                case "76-100" -> "76~100%";
                case "101-200" -> "101~200%";
                case "200+" -> "200% 초과";
                default -> u.incomeBracket();
            });
        }
        if (u.studentStatus()!=null && !"해당사항없음".equals(u.studentStatus()) && matchStudent(s,u.studentStatus())) {
            r.add("재학: " + u.studentStatus());
        }
        if (u.employmentStatus()!=null && matchEmployment(s,u.employmentStatus())) {
            r.add("취업상태: " + u.employmentStatus());
        }
        if (u.specialFlags()!=null && !u.specialFlags().isEmpty() && matchSpecial(s,u.specialFlags())) {
            for (String f: u.specialFlags()) {
                if (!"해당사항없음".equals(f)) r.add(f);
            }
        }
        if (u.industry()!=null && matchIndustry(s,u.industry())) {
            r.add("업종: " + u.industry());
        }
        return r;
    }
}
