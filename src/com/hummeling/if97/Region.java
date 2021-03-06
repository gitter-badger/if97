/*
 * Region.java
 *
 * This file is part of IF97.
 *
 * IF97 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * IF97 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with IF97. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2014 Hummeling Engineering BV (www.hummeling.com)
 */
package com.hummeling.if97;

import static com.hummeling.if97.Region4.*;
import static java.lang.Math.*;

/**
 * Abstract region class.
 *
 * @author Ralph Hummeling (<a
 * href="http://www.hummeling.com">www.hummeling.com</a>)
 */
abstract class Region {

// <editor-fold defaultstate="collapsed" desc="fields">
    private static final String NAME = "Region";
    //final String NAME = null;
    /**
     *
     */
    private static final double[] nB23 = new double[]{
        0.34805185628969e3,
        -.11671859879975e1,
        0.10192970039326e-2,
        0.57254459862746e3,
        0.13918839778870e2
    };
// </editor-fold>

    String getName() {
        return NAME;
    }

    /**
     * Get region as a function of specific enthalpy & specific entropy.
     *
     * @param h specific enthalpy
     * @param s specific entropy
     * @return region
     * @throws OutOfRangeException out-of-range exception
     */
    static Region getRegionHS(double h, double s) throws OutOfRangeException {

        Region region1 = new Region1(), region2 = new Region2(),
                region3 = new Region3(), region4 = new Region4();
        double T273 = 273.15,
                p1 = region1.pressureHS(h, s),
                T1 = region1.temperaturePH(p1, h) + 24e-3,
                //pSat273 = saturationPressureT(T273),
                //pSat623 = saturationPressureT(T623),
                h273 = region1.specificEnthalpyPT(saturationPressureT(T273), T273),
                s1 = region1.specificEntropyPT(100, T273);
        //s273 = region1.specificEntropyPT(pSat273, T273),
        //s623 = region1.specificEntropyPT(pSat623, T623),
        //s2bc = 5.85;
        double[] hB23limits = {2.563592004e3, 2.812942061e3},
                sB23limits = {5.048096828, 5.260578707};

        //System.out.println("p1: " + p1);
        //System.out.println("T1: " + T1);
        //System.out.println("h273: " + h273);
        //System.out.println("s1: " + s1);
        /*
         * Checks
         */
        //if (enthalpy < h273) {
        //    throw new OutOfRangeException(SPECIFIC_ENTHALPY, enthalpy, h273);
        //
        //} else if (entropy < s1) {
        //    throw new OutOfRangeException(SPECIFIC_ENTROPY, entropy, s1);
        //
        //} else if (T1 < T273) {
        //    throw new OutOfRangeException(TEMPERATURE, T1, T273);
        //}
        /*
         * Select Region
         */
        if (s <= 3.778281340) {
            // region 1, 3, or 4
            if (h <= specificEnthalpy1(s)) {
                return region4;

            } else if (h > specificEnthalpyB13(s)) {
                return region3;

            } else {
                return region1;
            }

        } else if (s <= IF97.sc) {
            // region 3 or 4
            if (h <= specificEnthalpy3a(s)) {
                return region4;

            } else {
                return region3;
            }

        } else if (s < 5.85) {
            if (h <= specificEnthalpy2c3b(s)) {
                return region4;

            } else {
                // region 2 or region 3
                if (h <= hB23limits[0] || s <= sB23limits[0]) {
                    return region3;

                } else if (h >= hB23limits[1] || s >= sB23limits[1]) {
                    return region2;

                } else if (hB23limits[0] < h && h < hB23limits[1]
                        && sB23limits[0] < s && s < sB23limits[1]) {

                    if (region2.pressureHS(h, s)
                            > pressureB23(temperatureB23HS(h, s))) {
                        return region3;

                    } else {
                        return region2;
                    }
                }
            }

        } else if (s <= 9.155759395) {
            if (h <= specificEnthalpy2ab(s)) {
                return region4;
            }
        }

        return region2;
    }

    static Region getRegionPH(double p, double h) throws OutOfRangeException {

        /*
         * Checks
         */
        //if (!validRangePT(pressure, temperature)) {
        //    return null;
        //}
        if (p < saturationPressureT(273.15)) {
            throw new OutOfRangeException(IF97.Quantity.p, p, saturationPressureT(273.15));
        }

        Region region1 = new Region1(), region2 = new Region2(),
                region3 = new Region3(), region4 = new Region4(),
                region5 = new Region5();
        double T623 = 623.15, T1073 = 1073.15,
                pSat623 = saturationPressureT(T623),
                Tsat = saturationTemperatureP(p),
                hSat1 = region1.specificEnthalpyPT(pSat623, T623),
                hSat2 = region2.specificEnthalpyPT(pSat623, T623),
                h1073 = region2.specificEnthalpyPT(p, T1073),
                pSat3 = saturationPressureH(h);


        /*
         * Select Region
         */
        if (h > region2.specificEnthalpyPT(p, T1073)) {
            if (p > 50) {
                throw new OutOfRangeException(IF97.Quantity.p, p, 50);
            }
            return region5;
        }

        if (p < pSat623) {
            // region 1, 4, or 2
            if (h < region1.specificEnthalpyPT(p, Tsat)) {
                return region1;

            } else if (h > region2.specificEnthalpyPT(p, Tsat)) {
                return region2;

            } else {
                return region4;
            }

        }
        if (hSat1 <= h && h <= hSat2) {
            // region 3 or 4
            if (p > pSat3) {
                return region3;
            } else {
                return region4;
            }
        }

        if (h <= region1.specificEnthalpyPT(p, T623)) {
            return region1;

        } else if (h > region2.specificEnthalpyPT(p, temperatureB23P(p))) {
            return region2;

        } else {
            return region3;
        }
    }

    static Region getRegionPT(IF97.UnitSystem unitSystem, double p, double T) throws OutOfRangeException {

        double press = IF97.convertToDefault(unitSystem.PRESSURE, p);
        double temp = IF97.convertToDefault(unitSystem.TEMPERATURE, T);

        Region region;
        try {
            region = getRegionPT(press, temp);

        } catch (OutOfRangeException e) {
            throw e.convertFromDefault(unitSystem);
        }

        return region;
    }

    static Region getRegionPT(double p, double T) throws OutOfRangeException {

        /*
         * Checks
         */
        if (p <= 0) {
            throw new OutOfRangeException(IF97.Quantity.p, p, 0);

        } else if (p > 100) {
            throw new OutOfRangeException(IF97.Quantity.p, p, 100);

        } else if (T < 273.15) {
            throw new OutOfRangeException(IF97.Quantity.T, T, 273.15);

        } else if (T > 1073.15 && p > 50) {
            throw new OutOfRangeException(IF97.Quantity.p, p, 50);

        } else if (T > 2073.15) {
            throw new OutOfRangeException(IF97.Quantity.T, T, 2073.15);
        }


        /*
         * Select Region
         */
        if (T > 1073.15) {
            return new Region5();

        } else if (T > 623.15) {
            if (p > pressureB23(T)) {
                return new Region3();

            } else if (p > 10) {
                return new Region2();

            } else {
                //return new Region2Meta();
                return new Region2();
            }
        } else {
            if (p > saturationPressureT(T)) {
                return new Region1();

            } else if (p > 10) {
                return new Region2();

            } else {
                //return new Region2Meta();
                return new Region2();
            }
        }
    }

    /**
     * Isobaric cubic expansion coefficient.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return isobaric cubic expansion coefficient [1/K]
     */
    //abstract double isobaricCubicExpansionCoefficientPH(double p, double h);
    /**
     * Isobaric cubic expansion coefficient.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return isobaric cubic expansion coefficient [1/K]
     */
    abstract double isobaricCubicExpansionCoefficientPT(double p, double T);

    /**
     * Isobaric cubic expansion coefficient.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return isobaric cubic expansion coefficient [1/K]
     */
    //abstract double isobaricCubicExpansionCoefficientRhoT(double rho, double T);
    /**
     * Isothermal compressibility.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return isothermal compressibility [1/MPa]
     */
    //abstract double isothermalCompressibilityPH(double p, double h);
    /**
     * Isothermal compressibility.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return isothermal compressibility [1/MPa]
     */
    abstract double isothermalCompressibilityPT(double p, double T);

    /**
     * Isothermal compressibility.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return isothermal compressibility [1/MPa]
     */
    //abstract double isothermalCompressibilityRhoT(double rho, double T);
    /**
     * Isothermal stress coefficient.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return isothermal stress coefficient
     */
    //abstract double isothermalStressCoefficientRhoT(double rho, double T);
    /**
     * Auxiliary equation for the boundary between regions 2 and 3.
     *
     * @param T temperature [K]
     * @return pressure [MPa]
     */
    static double pressureB23(double T) {
        return nB23[0] + nB23[1] * T + nB23[2] * T * T;
    }

    /**
     * Pressure as a function of specific enthalpy & specific entropy.
     *
     * @param h specific enthalpy [kJ/kg]
     * @param s specific entropy [kJ/kg-K]
     * @return pressure [MPa]
     */
    abstract double pressureHS(double h, double s);

    /**
     * Pressure as a function of density & temperature.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return pressure [MPa]
     */
    //abstract double pressureRhoT(double rho, double T);
    static double specificEnthalpy1(double s) {

        double eta = 0, sigma = s / 3.8;
        double[] x = {sigma - 1.09, sigma + 0.366e-4, 1700};
        double[][] IJn = {
            {0, 14, .332171191705237},
            {0, 36, .611217706323496e-3},
            {1, 3, -.882092478906822e1},
            {1, 16, -.455628192543250},
            {2, 0, -.263483840850452e-4},
            {2, 5, -.223949661148062e2},
            {3, 4, -.428398660164013e1},
            {3, 36, -.616679338856916},
            {4, 4, -.146823031104040e2},
            {4, 16, .284523138727299e3},
            {4, 24, -.113398503195444e3},
            {5, 18, .115671380760859e4},
            {5, 24, .395551267359325e3},
            {7, 1, -.154891257229285e1},
            {8, 4, .194486637751291e2},
            {12, 2, -.357915139457043e1},
            {12, 4, -.335369414148819e1},
            {14, 1, -.664426796332460},
            {14, 22, .323321885383934e5},
            {16, 10, .331766744667084e4},
            {20, 12, -.223501257931087e5},
            {20, 28, .573953875852936e7},
            {22, 8, .173226193407919e3},
            {24, 3, -.363968822121321e-1},
            {28, 0, .834596332878346e-6},
            {32, 6, .503611916682674e1},
            {32, 8, .655444787064505e2}
        };
        for (double[] ijn : IJn) {
            eta += ijn[2] * pow(x[0], ijn[0]) * pow(x[1], ijn[1]);
        }
        return eta * x[2];
    }

    static double specificEnthalpy2ab(double s) {

        double eta = 0;
        double[] x = {5.21 / s - 0.513, s / 9.2 - 0.524, 2800};
        double[][] IJn = {
            {1, 8, -.524581170928788e3},
            {1, 24, -.926947218142218e7},
            {2, 4, -.237385107491666e3},
            {2, 32, .210770155812776e11},
            {4, 1, -.239494562010986e2},
            {4, 2, .221802480294197e3},
            {7, 7, -.510472533393438e7},
            {8, 5, .124981396109147e7},
            {8, 12, .200008436996201e10},
            {10, 1, -.815158509791035e3},
            {12, 0, -.157612685637523e3},
            {12, 7, -.114200422332791e11},
            {18, 10, .662364680776872e16},
            {20, 12, -.227622818296144e19},
            {24, 32, -.171048081348406e32},
            {28, 8, .660788766938091e16},
            {28, 12, .166320055886021e23},
            {28, 20, -.218003784381501e30},
            {28, 22, -.787276140295618e30},
            {28, 24, .151062329700346e32},
            {32, 2, .795732170300541e7},
            {32, 7, .131957647355347e16},
            {32, 12, -.325097068299140e24},
            {32, 14, -.418600611419248e26},
            {32, 24, .297478906557467e35},
            {36, 10, -.953588761745473e20},
            {36, 12, .166957699620939e25},
            {36, 20, -.175407764869978e33},
            {36, 22, .347581490626396e35},
            {36, 28, -.710971318427851e39}
        };
        for (double[] ijn : IJn) {
            eta += ijn[2] * pow(x[0], ijn[0]) * pow(x[1], ijn[1]);
        }
        return exp(eta) * x[2];
    }

    static double specificEnthalpy2c3b(double s) {

        double eta = 0, sigma = s / 5.9;
        double[] x = {sigma - 1.02, sigma - 0.726, 2800};
        double[][] IJn = {
            {0, 0, .104351280732769e1},
            {0, 3, -.227807912708513e1},
            {0, 4, .180535256723202e1},
            {1, 0, .420440834792042},
            {1, 12, -.105721244834660e6},
            {5, 36, .436911607493884e25},
            {6, 12, -.328032702839753e12},
            {7, 16, -.678686760804270e16},
            {8, 2, .743957464645363e4},
            {8, 20, -.356896445355761e20},
            {12, 32, .167590585186801e32},
            {16, 36, -.355028625419105e38},
            {22, 2, .396611982166538e12},
            {22, 32, -.414716268484468e41},
            {24, 7, .359080103867382e19},
            {36, 20, -.116994334851995e41}
        };
        for (double[] ijn : IJn) {
            eta += ijn[2] * pow(x[0], ijn[0]) * pow(x[1], ijn[1]);
        }
        return pow(eta, 4) * x[2];
    }

    static double specificEnthalpy3a(double s) {

        double eta = 0, sigma = s / 3.8;
        double[] x = {sigma - 1.09, sigma + 0.366e-4, 1700};
        double[][] IJn = {
            {0, 1, .822673364673336},
            {0, 4, .181977213534479},
            {0, 10, -.112000260313624e-1},
            {0, 16, -.746778287048033e-3},
            {2, 1, -.179046263257381},
            {3, 36, .424220110836657e-1},
            {4, 3, -.341355823438768},
            {4, 16, -.209881740853565e1},
            {5, 20, -.822477343323596e1},
            {5, 36, -.499684082076008e1},
            {6, 4, .191413958471069},
            {7, 2, .581062241093136e-1},
            {7, 28, -.165505498701029e4},
            {7, 32, .158870443421201e4},
            {10, 14, -.850623535172818e2},
            {10, 32, -.317714386511207e5},
            {10, 36, -.945890406632871e5},
            {32, 0, -.139273847088690e-5},
            {32, 6, .631052532240980}
        };
        for (double[] ijn : IJn) {
            eta += ijn[2] * pow(x[0], ijn[0]) * pow(x[1], ijn[1]);
        }
        return eta * x[2];
    }

    static double specificEnthalpyB13(double s) {

        double eta = 0, sigma = s / 3.8;
        double[] x = {sigma - 0.884, sigma - 0.864, 1700};
        double[][] IJn = {
            {0, 0, .913965547600543},
            {1, -2, -.430944856041991e-4},
            {1, 2, .603235694765419e2},
            {3, -12, .117518273082168e-17},
            {5, -4, .220000904781292},
            {6, -3, -.690815545851641e2}
        };
        for (double[] ijn : IJn) {
            eta += ijn[2] * pow(x[0], ijn[0]) * pow(x[1], ijn[1]);
        }
        return eta * x[2];
    }

    /**
     * Specific enthalpy.
     *
     * @param pressure pressure [MPa]
     * @param T temperature [K]
     * @return specific enthalpy [kJ/kg]
     */
    abstract double specificEnthalpyPT(double p, double T);

    /**
     * Specific enthalpy.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return specific enthalpy [kJ/kg]
     */
    //abstract double specificEnthalpyRhoT(double rho, double T);
    /**
     * Specific entropy.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return specific entropy [kJ/kg-K]
     */
    //abstract double specificEntropyPH(double p, double h);
    /**
     * Specific entropy.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return specific entropy [kJ/kg-K]
     */
    abstract double specificEntropyPT(double p, double T);

    /**
     * Specific entropy.
     *
     * @param rho density [kg/m&sup3;]
     * @param h specific enthalpy [kJ/kg]
     * @return specific entropy [kJ/kg-K]
     */
    //abstract double specificEntropyRhoH(double rho, double h);
    /**
     * Specific entropy.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return specific entropy [kJ/kg-K]
     */
    abstract double specificEntropyRhoT(double rho, double T);

    /**
     * Specific internal energy.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return specific internal energy [kJ/kg]
     */
    //abstract double specificInternalEnergyPH(double p, double h);
    /**
     * Specific internal energy.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return specific internal energy [kJ/kg]
     */
    abstract double specificInternalEnergyPT(double p, double T);

    /**
     * Specific internal energy.
     *
     * @param rho density [kg/m&sup3;]
     * @param h specific enthalpy [kJ/kg]
     * @return specific internal energy [kJ/kg]
     */
    //abstract double specificInternalEnergyRhoH(double rho, double h);
    /**
     * Specific internal energy.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return specific internal energy [kJ/kg]
     */
    //abstract double specificInternalEnergyRhoT(double rho, double T);
    /**
     * Specific isobaric heat capacity.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return specific isobaric heat capacity [kJ/kg-K]
     */
    //abstract double specificIsobaricHeatCapacityPH(double p, double h);
    /**
     * Specific isobaric heat capacity.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return specific isobaric heat capacity [kJ/kg-K]
     */
    abstract double specificIsobaricHeatCapacityPT(double p, double T);

    /**
     * Specific isobaric heat capacity.
     *
     * @param rho density [kg/m&sup3;]
     * @param h specific enthalpy [kJ/kg]
     * @return specific isobaric heat capacity [kJ/kg-K]
     */
    //abstract double specificIsobaricHeatCapacityRhoH(double rho, double h);
    /**
     * Specific isobaric heat capacity.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return specific isobaric heat capacity [kJ/kg-K]
     */
    //abstract double specificIsobaricHeatCapacityRhoT(double rho, double T);
    /**
     * Specific isochoric heat capacity.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return specific isochoric heat capacity [kJ/kg-K]
     */
    //abstract double specificIsochoricHeatCapacityPH(double p, double h);
    /**
     * Specific isochoric heat capacity.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return specific isochoric heat capacity [kJ/kg-K]
     */
    abstract double specificIsochoricHeatCapacityPT(double p, double T);

    /**
     * Specific isochoric heat capacity.
     *
     * @param rho density [kg/m&sup3;]
     * @param h specific enthalpy [kJ/kg]
     * @return specific isochoric heat capacity [kJ/kg-K]
     */
    //abstract double specificIsochoricHeatCapacityRhoH(double rho, double h);
    /**
     * Specific isochoric heat capacity.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return specific isochoric heat capacity [kJ/kg-K]
     */
    //abstract double specificIsochoricHeatCapacityRhoT(double rho, double T);
    /**
     * Specific volume.
     *
     * @param h specific enthalpy [kJ/kg]
     * @param s specific entropy [kJ/kg-K]
     * @return specific volume [m&sup3;/kg]
     */
    //abstract double specificVolumeHS(double h, double s);
    /**
     * Specific volume.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return specific volume [m&sup3;/kg]
     */
    //abstract double specificVolumePH(double p, double h);
    /**
     * Specific volume as a function of pressure & specific entropy.
     *
     * @param p pressure [MPa]
     * @param s specific entropy [kJ/kg-K]
     * @return specific volume [m&sup3;/kg]
     */
    //abstract double specificVolumePS(double p, double s);
    /**
     * Specific volume.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return specific volume [m&sup3;/kg]
     */
    abstract double specificVolumePT(double p, double T);

    /**
     * Speed of sound.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return speed of sound [m/s]
     */
    //abstract double speedOfSoundPH(double p, double h);
    /**
     * Speed of sound.
     *
     * @param p pressure [MPa]
     * @param T temperature [K]
     * @return speed of sound [m/s]
     */
    abstract double speedOfSoundPT(double p, double T);

    /**
     * Speed of sound.
     *
     * @param rho density [kg/m&sup3;]
     * @param T temperature [K]
     * @return speed of sound [m/s]
     */
    //abstract double speedOfSoundRhoT(double rho, double T);
    /**
     * Auxiliary equation for the boundary between regions 2 and 3.
     *
     * @param p pressure [MPa]
     * @return temperature [K]
     */
    static double temperatureB23P(double p) {
        return nB23[3] + sqrt((p - nB23[4]) / nB23[2]);
    }

    /**
     * Auxiliary equation for the boundary between regions 2 and 3.
     *
     * @param h specific enthalpy [kJ/kg]
     * @param s specific entropy [kJ/kg-K]
     * @return temperature [K]
     */
    static double temperatureB23HS(double h, double s) {

        double theta = 0, eta = h / 3e3, sigma = s / 5.3;
        double[] x = new double[]{eta - 0.727, sigma - 0.864, 900};
        double[][] IJn = new double[][]{
            {-12, 10, .629096260829810e-3},
            {-10, 8, -.823453502583165e-3},
            {-8, 3, .515446951519474e-7},
            {-4, 4, -.117565945784945e1},
            {-3, 3, .348519684726192e1},
            {-2, -6, -.507837382408313e-11},
            {-2, 2, -.284637670005479e1},
            {-2, 3, -.236092263939673e1},
            {-2, 4, .601492324973779e1},
            {0, 0, .148039650824546e1},
            {1, -3, .360075182221907e-3},
            {1, -2, -.126700045009952e-1},
            {1, 10, -.122184332521413e7},
            {3, -2, .149276502463272},
            {3, -1, .698733471798484},
            {5, -5, -.252207040114321e-1},
            {6, -6, .147151930985213e-1},
            {6, -3, -.108618917681849e1},
            {8, -8, -.936875039816322e-3},
            {8, -2, .819877897570217e2},
            {8, -1, -.182041861521835e3},
            {12, -12, .261907376402688e-5},
            {12, -1, -.291626417025961e5},
            {14, -12, .140660774926165e-4},
            {14, 1, .783237062349385e7}
        };

        for (double[] ijn : IJn) {
            theta += ijn[2] * pow(x[0], ijn[0]) * pow(x[1], ijn[1]);
        }

        return theta * x[2];
    }

    /**
     * Temperature.
     *
     * @param h specific enthalpy [kJ/kg]
     * @param s specific entropy [kJ/kg-K]
     * @return temperature [K]
     */
    abstract double temperatureHS(double h, double s);

    /**
     * Temperature.
     *
     * @param p pressure [MPa]
     * @param h specific enthalpy [kJ/kg]
     * @return temperature [K]
     */
    abstract double temperaturePH(double p, double h);

    /**
     * Temperature as a function of pressure & specific entropy.
     *
     * @param p pressure [MPa]
     * @param s specific entropy [kJ/kg-K]
     * @return temperature [K]
     */
    //abstract double temperaturePS(double p, double s);
    /**
     * Temperature.
     *
     * @param rho density [kg/m&sup3;]
     * @param h specific enthalpy [kJ/kg]
     * @return temperature [K]
     */
    //abstract double temperatureRhoH(double rho, double h);
    /**
     * Vapour fraction.
     *
     * @param h specific enthalpy [kJ/kg]
     * @param s specific entropy [kJ/kg-K]
     * @return vapour fraction [-]
     */
    abstract double vapourFractionHS(double h, double s);
}
