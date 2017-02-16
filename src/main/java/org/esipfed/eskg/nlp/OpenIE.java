/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esipfed.eskg.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


import edu.knowitall.openie.Argument;
import edu.knowitall.openie.Instance;
import edu.knowitall.openie.OpenIE;
import edu.knowitall.tool.parse.ClearParser;
import edu.knowitall.tool.postag.ClearPostagger;
import edu.knowitall.tool.srl.ClearSrl;
import edu.knowitall.tool.tokenize.ClearTokenizer;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import scala.collection.JavaConversions;
import scala.collection.Seq;

public class OpenIE {

	public OpenIE() {
		// TODO Auto-generated constructor stub
	}

	static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		SentenceDetector sentenceDetector = null;
		InputStream modelIn = null;

		try {
			//need to change this to the resource folder
			File fi = new File("C:/Users/Yongyao/Desktop/papers/reverb/en-sent.bin");
			
			modelIn = new FileInputStream(fi);
			final SentenceModel sentenceModel = new SentenceModel(modelIn);
			modelIn.close();
			sentenceDetector = new SentenceDetectorME(sentenceModel);
		}
		catch (final IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("---Started---");
		OpenIE openIE = new OpenIE(new ClearParser(new ClearPostagger(new ClearTokenizer())), new ClearSrl(), false, false);

		//any text file that contains English sentences would work
		File file = new File("C:/Users/Yongyao/Desktop/papers/reverb/test.txt");
		String text = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);

		String sentences[]= sentenceDetector.sentDetect(text);
		for(int i=0; i<sentences.length; i++)
		{

			Seq<Instance> extractions = openIE.extract(sentences[i]);

			List<Instance> list_extractions = JavaConversions.seqAsJavaList(extractions);


			for(Instance instance : list_extractions) {
				StringBuilder sb = new StringBuilder();

				sb.append(instance.confidence())
				.append('\t')
				.append(instance.extr().context())
				.append('\t')
				.append(instance.extr().arg1().text())
				.append('\t')
				.append(instance.extr().rel().text())
				.append('\t');

				List<Argument> list_arg2s = JavaConversions.seqAsJavaList(instance.extr().arg2s());
				for(Argument argument : list_arg2s) {
					sb.append(argument.text()).append("; ");
				}

				System.out.println(sb.toString());
			}
		}

	}

}
