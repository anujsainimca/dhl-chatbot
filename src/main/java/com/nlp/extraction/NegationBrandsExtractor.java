package com.nlp.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NegationBrandsExtractor {

	public String getNegatedBrands(String question) {

		String regexp = "((dont show |dont like |don't show |don't like |except |dont show me |don't show me |do not show |do not show me ){1,2}((Apple|iphone|Vivo|Samsung|OPPO|LYF|Asus|LeEco|Gionee|Panasonic|Motorola|Nokia|Micromax|Lenovo|Google|Sony|HTC|INTEX|BlackBerry|Honor|Mido|Videocon|LG|Yu|Infocus|Swipe|iBall|XOLO|Microsoft|Coolpad|Karbonn|Reach|Ziox|LAVA|BLACKBEAR|Xccess|Vphone S8|HPL|ZEN|Celkon|Onida|L'Exotique AIEK|Rage|XISOM|Sansui|Spice|Trio|Alcatel|BLU|Brandsdaddy|Rodeio|ZTE|I Kall|Jivi|Itel|i-smart|Kechaoda|L'Exotique|Philips|Maya|Peace|Gamma|A&K|MELBON|TARA|Salora|Reliance|SICCOO|Kara|Aura|Callbar|Darago|iKall|Good One|SICT|Hitech|Seniorworld|kestrel|UBLU|Heemax|U- Touch|Tashan|AIEK|Tecoze|GreenBerry|mPhone|Nuvo|Canvas|Speed Mobiles|Cheers|Vbera|Mafe|Go Hello|Xolt|Stringss|Champion|Nikcron|Aqua|Vista|Haier|ISUN|Qtel|OBI|Zopo|MIG51|Huawei|Talk 3|Hotpary|NXI|BSNL Penta|G-vill|Usha Shriram|nanoTel|Doel){1,3}( |or|and|,){0,3}){1,3})";
		Pattern p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
		String price = "";
		Matcher m = p.matcher(question);
		while (m.find()) {
			if (m.group().trim().length() > 1) {
				if (m.group().length() > price.length())
					price = (m.group());

			}
		}
		return price;

	}

	public static void main(String args[]) {
		NegationBrandsExtractor pe = new NegationBrandsExtractor();
		String question = "show me all projects from repository except jira.";
		System.out.println(pe.getNegatedBrands(question));

	}
}
