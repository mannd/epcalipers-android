/*
 * *
 *  Copyright (C) ${YEAR} EP Studios, Inc.
 * www.epstudiossoftware.com
 *
 * Created by ${USER} on ${DATE}.
 *
 * This file is part of ${PROJECT_NAME}.
 *
 *     ${PROJECT_NAME} is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ${PROJECT_NAME} is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ${PROJECT_NAME}.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.epstudios.epcalipers;


import android.app.Activity;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class QtcCalculator {
    public enum QtcFormula {
        qtcBzt,
        qtcFrm,
        qtcHdg,
        qtcFrd,
        qtcAll  // calculate all the above QTcs
    }

    private QtcFormula formula;
    public void setFormula(QtcFormula value) {
	formula = value;
    }
    
    private final DecimalFormat decimalFormat;
    // use to iterate through all formulas
    private final QtcFormula[] allFormulas = {
            QtcFormula.qtcBzt,
            QtcFormula.qtcFrm,
            QtcFormula.qtcHdg,
            QtcFormula.qtcFrd
    };

    private final Map<QtcFormula, String> formulaNames;

    QtcCalculator(QtcFormula formula, Activity activity) {
        this.formula = formula;
        decimalFormat = new DecimalFormat("@@@##");
        formulaNames = new HashMap<>();
        formulaNames.put(QtcFormula.qtcBzt, activity.getString(R.string.bazett_formula));
        formulaNames.put(QtcFormula.qtcFrm, activity.getString(R.string.framingham_formula));
        formulaNames.put(QtcFormula.qtcHdg, activity.getString(R.string.hodges_formula));
        formulaNames.put(QtcFormula.qtcFrd, activity.getString(R.string.fridericia_formula));
        formulaNames.put(QtcFormula.qtcAll, activity.getString(R.string.all_formulas));
    }

    public String calculate(double qtInSec, double rrInSec,
                            boolean convertToMsec, String units) {
        QtcFormula[] formulas = {formula};
        if (formula == QtcFormula.qtcAll) {
            formulas = allFormulas;
        }
        double qt = qtInSec;
        double meanRR = rrInSec;
        if (convertToMsec) {
            qt *= 1000.0;
            meanRR *= 1000.0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Mean RR = ").append(decimalFormat.format(meanRR)).append(" ")
                .append(units).append("\nQT = ").append(decimalFormat.format(qt)).append(" ").append(units);
        for (QtcFormula formula1 : formulas) {
            double qtc = calculate(qtInSec, rrInSec, formula1);
            if (convertToMsec) {
                qtc *= 1000.0;
            }
            sb.append("\nQTc = ").append(decimalFormat.format(qtc)).append(" ")
                    .append(units).append(" (").append(formulaNames.get(formula1)).append(" formula)");
//            result += "\nQTc = " + decimalFormat.format(qtc) + " " +
//                    units + " (" + formulaNames.get(formulas[i]) + " formula)";
        }
        return sb.toString();
    }

    private double calculate(double qtInSec, double rrInSec, QtcFormula formula) {
        double qtc;
        switch (formula) {
            case qtcBzt:
                qtc = calculateQtcBzt(qtInSec, rrInSec);
                break;
            case qtcFrm:
                qtc = calculateQtcFrm(qtInSec, rrInSec);
                break;
            case qtcHdg:
                qtc = calculateQtcHdg(qtInSec, rrInSec);
                break;
            case qtcFrd:
                qtc = calculateQtcFrd(qtInSec, rrInSec);
                break;
            default:
                qtc = -1.0;
                break;
        }
        return qtc;
    }

    private double calculateQtcBzt(double qtInSec, double rrInSec) {
        return qtInSec / Math.pow(rrInSec, 0.5);
    }

    private double calculateQtcFrm(double qtInSec, double rrInSec) {
        return qtInSec + 0.154 * (1 - rrInSec);
    }

    private double calculateQtcHdg(double qtInSec, double rrInSec) {
        return qtInSec + 0.00175 * (60.0 / rrInSec - 60);
    }

    private double calculateQtcFrd(double qtInSec, double rrInSec) {
        return qtInSec / Math.pow(rrInSec, 1 / 3.0);
    }
}
