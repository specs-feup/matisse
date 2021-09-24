/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.MatlabProcessor.oneliners;

public enum OneLinersResource {
    TEST2(" a.b(1).c = x().y.z();"),
    TEST3("length skellen(k)", "length 'skellen(k)'"),
    TEST4("if a + a a; end", "if a + a\n   a;\nend"),
    TEST5("if [[x]] 1; end", "if [[x]]\n   1;\nend"),
    TEST7("if a ((x)); end", "if a((x))\nend"),
    TEST9("freq = [ 1 1:l (l-1):-1:1 ];", "freq = [1, 1:l, (l - 1):-1:1];"),
    TEST10("function ii = end(tt,kk,nn);", "function [ii] = end(tt, kk, nn)\nend"),
    TEST12("say suffices).", "say 'suffices).'"),
    TEST13("zm(1:nz-1)=z(1:nz-1)-.5*dz(1:nz-1)'';", "zm(1:nz - 1) = z(1:nz - 1) - .5 * dz(1:nz - 1)'';"),
    TEST14("zi = [(iseg(id + 1) - 1):-1:(iseg(id) + 1)];\n" +
	    "%segment coordinates"),
    TEST15("keybar(:,:,1) = [maxhue:-maxhue/nhues:0]';", "keybar(:, :, 1) = [maxhue:-maxhue / nhues:0]';"),
    TEST16("draw(gcells(i), gv, struct ('x',geomT, 'u',0*geomT,...\n 'colorfield',colorfield, 'shrink',01));",
	    "draw(gcells(i), gv, struct('x', geomT, 'u', 0 * geomT, 'colorfield', colorfield, 'shrink', 1));"),
    TEST17("[AC, NumContours] = DecodeSymbol(AC, [256:-1:1]);"),
    TEST18("if(isfield(device.(types{n}), color))", "if isfield(device.(types{n}), color)"),
    TEST19("h = varargin{2}(:)'"),
    TEST20("nn.dW{i} = (d{i + 1}(:, 2:end)' * nn.a{i}) / size(d{i + 1}, 1);"), // transpose of composable token
    TEST21("try fx = fx + X(dim.n + 1:2 * dim.n); end", "try\n   fx = fx + X(dim.n + 1:2 * dim.n);\nend"),
    TEST22("meanValue = mean(vectors')'"),
    TEST23("try h; catch h = 0; end", "try\n   h;\ncatch\n   h = 0;\nend"),
    TEST24("try h; catch h; end", "try\n   h;\ncatch h\nend"),
    TEST25("hax = handles.(['axlegendline'  num2str(ilegend)]);", "hax = handles.(['axlegendline', num2str(ilegend)]);"),
    TEST26("x.(key)(end + 1) = xmlstruct(x.children(c))"),
    TEST27("reshape(temp_mix.centres', 1, ncentres * dim_target);"),
    TEST28("if -u(1) ~= u(2)\nend"),
    TEST29("if -handles.uncVect(val, 1) == handles.uncVect(val, 2)"),
    TEST30("if -newlb == handles.cur_uncVect(idx, 2)"),
    TEST31("if -yi*val_f>=87", "if -yi * val_f >= 87"),
    TEST32("classdef (ConstructOnLoad) Employee < handle\nend"),
    TEST33("if listOfTerms{objRow}{objTerm}(1) == '-'\nend"),
    TEST34(" a  . b (1) . c = x().y;", "a.b(1).c = x().y;"),
    TEST35("if a{1} .('a') == 'b' 4, end", "if a{1}.('a') == 'b'\n   4\nend"),
    TEST36("y = DCM.Y.y';"),
    TEST37(" a{1}(:)'"),
    TEST38("for index = 2:9 if chan_names(elec,index) == 0 x = 1\nend;\nend;", "for index = 2:9\n" +
	    "   if chan_names(elec, index) == 0\n" +
	    "      x = 1\n" +
	    "   end\n" +
	    "end"),
    TEST39("if aboxes{j}{i}(keep(k), end) > -0.9"),
    TEST40("for index = 2:9 if chan_names(elec,index) == 0 chan_names(elec,index)=' '; end; end;", "for index = 2:9\n" +
	    "   if chan_names(elec, index) == 0\n" +
	    "      chan_names(elec, index) = ' ';\n" +
	    "   end\n" +
	    "end"),
    TEST41("if listOfTerms{objRow}{objTerm}(1) == '-'"),
    TEST42(
	    "cfg_tbxvol_spike_mask.help  = {'A very crude method to detect spikes.' \n" +
		    "    ['Images will be weighted by the mask image or its inverse (1-mask). ', ... \n" +
		    "    'Afterwards, for each slice the number of voxels exceeding the ', ...\n" +
		    "    'specified threshold will be counted. Images will be marked ''bad'' ', ...\n" +
		    "    'if more than a specified percentage of voxels per slice exceed the threshold.']\n" +
		    "    'This method is useful to detect rare spikes on a set of e.g.'\n" +
		    "    'standard deviation images of a time series. '};",
	    "cfg_tbxvol_spike_mask.help = {'A very crude method to detect spikes.'; ['Images will be weighted by the mask image or its inverse (1-mask). ', 'Afterwards, for each slice the number of voxels exceeding the ', 'specified threshold will be counted. Images will be marked ''bad'' ', 'if more than a specified percentage of voxels per slice exceed the threshold.'], 'This method is useful to detect rare spikes on a set of e.g.', 'standard deviation images of a time series. '};"),
    TEST43("Jold = 10000000000000000000;"),
    TEST44("if imitationParams.hasJustRespawned imitationParams = resetImitationParams(imitationParams); end",
	    "if imitationParams.hasJustRespawned\n   imitationParams = resetImitationParams(imitationParams);\nend"),
    TEST45("classdef BaseUnits\n" +
	    "   %BASEUNITS provides unit support for dimensioned quantities\n" +
	    "   enumeration\n   end\nend"),
    TEST46("if length(p)==1 for i=1:N S(i,i)=p; end;\nend", "if length(p) == 1\n" +
	    "   for i = 1:N\n" +
	    "      S(i, i) = p;\n" +
	    "   end\n" +
	    "end"),
    TEST47("if -g.clustnums > MAXTOPOS"),
    TEST48("if -lo > hi"),
    TEST49(
	    "for y=1:1 for j=1:1 for i=1:3 figure(i) ; clf reset; plot_map_replacemany(i,fmins(i),fmaxs(i),years(y),ratios(j),0.5,'eleven','change'); end; end ; end;",
	    "for y = 1:1\r\n"
		    +
		    "   for j = 1:1\n"
		    +
		    "      for i = 1:3\n"
		    +
		    "         figure(i);\n"
		    +
		    "         clf 'reset';\n"
		    +
		    "         plot_map_replacemany(i, fmins(i), fmaxs(i), years(y), ratios(j), 0.5, 'eleven', 'change');\n"
		    +
		    "      end\n" +
		    "   end\n" +
		    "end"),
    TEST50("if srced > src.count srced = src.count; end", "if srced > src.count\n" +
	    "   srced = src.count;\n" +
	    "end"),
    TEST51(
	    "cfg_tbxvol_spike_mask.help  = {'A very crude method to detect spikes.' \n" +
		    "    ['Images will be weighted by the mask image or its inverse (1-mask). ', ... \n" +
		    "    'Afterwards, for each slice the number of voxels exceeding the ', ...\n" +
		    "    'specified threshold will be counted. Images will be marked ''bad'' ', ...\n" +
		    "    'if more than a specified percentage of voxels per slice exceed the threshold.']\n" +
		    "    'This method is useful to detect rare spikes on a set of e.g.'\n" +
		    "    'standard deviation images of a time series. '};",
	    "cfg_tbxvol_spike_mask.help = {'A very crude method to detect spikes.'; ['Images will be weighted by the mask image or its inverse (1-mask). ', 'Afterwards, for each slice the number of voxels exceeding the ', 'specified threshold will be counted. Images will be marked ''bad'' ', 'if more than a specified percentage of voxels per slice exceed the threshold.'], 'This method is useful to detect rare spikes on a set of e.g.', 'standard deviation images of a time series. '};"),
    TEST52("function set.T(obj, value)", "function set.T(obj, value)\nend"),
    TEST53("function y 1;", "function y()\n   1;\nend"),
    TEST54("format short;", "format 'short';"),
    TEST55(
	    "classdef  ...\n" +
		    "        ( ...\n" +
		    "          Hidden = false, ...          \n" +
		    "          InferiorClasses = {}, ...    \n" +
		    "          ConstructOnLoad = false, ... \n" +
		    "          Sealed = false ...           \n" +
		    "        ) ProjectiveTransform < handle\nend",
	    "classdef (Hidden = false, InferiorClasses = {}, ConstructOnLoad = false, Sealed = false) ProjectiveTransform < handle\nend"),
    TEST56("A = [+1 -2]", "A = [+1, -2]");

    private final String input;
    private final String expected;

    private OneLinersResource(String input, String expected) {
	this.input = input;
	this.expected = expected;
    }

    private OneLinersResource(String input) {
	this.input = input;
	this.expected = input;
    }

    public String getExpected() {
	return expected;
    }

    public String getInput() {
	return input;
    }
}
