#!/usr/bin/env python

# Scott Hazelhurst, 2016
# Creates a PDF report for QC

# Tested under both Python 2.7 and 3.5.2
# 
# Scott Hazelhurst on behalf of the H3ABioinformatics Network Consortium
# December 2016
# (c) Released under GPL v.2


from __future__ import print_function

import argparse
import sys
import os



# check if we are being called from the command line or as a template
# from Nextflow. If  as a template from Nextflow, then the nextflow process
# must define BASE
# NB: only for a Nextflow template -- you could also directly call from a Nextflow
# script if this script is on the path -- in which case the parameters should be passed
if len(sys.argv)==1:
   sys.argv = [sys.argv[0],"$orig","$base","$cbim","$cfam","$missingvhetpdf","$mafpdf","$dupf","$fsex","$indmisspdf","$snpmisspdf"]


def parseArguments():
    parser=argparse.ArgumentParser()
    parser.add_argument('orig', type=str),
    parser.add_argument('base', type=str),
    parser.add_argument('cbim', metavar='CBIM', type=str,help='clean bim'),
    parser.add_argument('cfam', metavar='CFAM', type=str,help='cleanfam'),
    parser.add_argument('missingvhetpdf', type=str),
    parser.add_argument('mafpdf', type=str),
    parser.add_argument('dupf', type=str),
    parser.add_argument('fsex', type=str),
    parser.add_argument('indmisspdf', type=str),
    parser.add_argument('snpmisspdf', type=str),        
    args = parser.parse_args()
    return args

args = parseArguments()
pdict = vars(args)

template='''
*-documentclass[11pt]{article}

*-usepackage{a4wide}
*-usepackage{graphicx}
*-usepackage{url}

*-title{Quality control report for %(base)s}

*-author{H3Agwas QC Pipeline}

*-newcommand{*-ourfig}[3]{*-begin{figure}[ht]*-begin{center}*-includegraphics[scale=0.6]{#3} *-end{center} *-caption{#2 [File is #3]}  *-label{#1}*-end{figure}}
*-begin{document}

*-maketitle

*-section{Introduction}

The input file for this analysis was *-emph{%(base)s}. This data includes:
*-begin{itemize}
*-item %(numrsnps)s SNPs
*-item %(numrfam)s  participants
*-end{itemize}

*-noindent
The final, cleaned result contains:
*-begin{itemize}
*-item %(numcsnps)s SNPs
*-item %(numcfam)s  participants
*-end{itemize}


*-subsection*{Approach}

The pipeline takes an incremental approach to QC, trading extra
computation time in order to achieve high quality while removing as
few data as possible. Rather than applying all cut-offs at once, we
incrementally apply cutoffs (for example, removing really badly
genotyped SNPs before checking for heterozygosity wil result in fewer individuals
failing heterozygosity checks).


*-section{Initial QC}

*-begin{enumerate}
*-item There were %(numdups)s duplicate SNPs. The file with them (if any) is called {*-em %(dupf)s}.
*-item %(numfailedsex)s individuals had discordant sex information -- further information can be found in {*-em %(fsex)s}.
*-end{enumerate}


*-section{Heterozygosity check}

Levels of heterozygosity were examined. Figure~*-ref{fig:missvhet} shows plots of heterozygosity versus
individual missingness (i.e., the number of SNPs missing per
individual).  Levels of heterozygosity should be between the ranges
given -- anything higher may indicate that there is sample
contamination, lower may indicate inbreeding. However, you need to
apply your mind to the data. Missingness should be low.

*-ourfig{fig:missvhet}{Missingness versus heterozygosity}{*-detokenize{%(missingvhetpdf)s}}

Individuals out of range heterozygosity were removed: first
SNPs with a genotyping failure rate of over 10 per cent were removed,
and then heterozysgosity checked. Any indviduals with heterozygosity:

*-begin{itemize}
*-item less than ${params.cut_het_low} are removed. This may indicate inbreeding.
*-item greater than ${params.cut_het_high} are removed. This may indicate sample contamination.
*-end{itemize}
 
*-noindent
Overall %(numhetrem)s individuals were removed. These individuals, if any, can be found in the file *-url{$misshetremf}.

*-noindent
Figure *-ref{fig:snpmiss} shows the spread of missingness per SNP across the sample, whereas *-ref{fig:indmiss} show the spread of missingness per individual across the sample these should be compared.

*-ourfig{fig:snpmiss}{SNP missingness}{*-detokenize{%(snpmisspdf)s}}

*-ourfig{fig:indmiss}{Missingness per indvididual}{*-detokenize{%(indmisspdf)s}}







*-section{Minor Allele Frequency Spread}

Figure~*-ref{fig:maf} shows the cumulative distribution of minor
allele frequency in the data. The MAF cut-off should be chosen high enough that you are sure that the variants you are seeing are real (so this would depend on the size of the sample). You have chosen a cut off of ${params.cut_maf}.


*-ourfig{fig:maf}{Minor allele frequency distribution}{*-detokenize{%(mafpdf)s}}


*-section{Differences between cases and controls}

We do not expect there to be large, observable macro-scale differences between cases and controls. Great caution needs to be taken in this case. 

We compute for each SNP the missingness in the cases, and the
missingness in the controls, and the corresponding p-value describing
the difference in missingness. 

We expect very few SNPs to have highly significant differences. Where
many SNPs with very highly significant p-values are found, great care
should be taken. Figure~*-ref{fig:diffP} plots the differences between
cases and controls, showing the SNP-wise p-value, unadjusted for multiple testing

*-ourfig{fig:diffP}{The plot shows for each (log) level of significance, the number of SNPs with that p-value}{*-detokenize{$diffmisspdf}}

For removal of SNPs, we compute the p-value adjusted for multiple testing, by performing permutation testing (1000 rounds) using the PLINK mperm maxT option.
SNPs are removed from the data set if their adjusted (EMP2) differential missingness p-value is less than ${params.cut_diff_miss}. The SNPs that are removed can be
found in the file *-url{$diffmiss}



Figure~*-ref{fig:pca} shows a principal component analysis of the
data, identifying the cases and controls. Should the cases and
controls cluster differ signficantly, something is likely wrong.
Moreover should there be any significant clusters or outliers, association
testing should take into account stratification. Statistical testing could also
be done.

*-ourfig{fig:pca}{Principal Component Analysis of Cases Versus Controls}{*-detokenize{$pcapdf}}

*-section{Hardy-Weinberg Equilibrium}

Deviation for Hardy-Weinberg Equilibrium (HWE) may indicate sample contamination. However, this need not apply to cases, nor in a situation where there is admixture. For each SNP, we compute  the probability of the null hypothesis (that  the deviation from HWE is by chance alone).  Figure~*-ref{fig:hwe} shows a plot of the corresponding p-value versus the frequency of occurrence.

*-ourfig{fig:hwe}{The plot shows for each level of significance, the number of SNPs with H
WE p-value}{*-detokenize{$hwepdf}}


*-section{Final filtering}

The details of the final filtering can be found in the Nextflow script. Note that the exact ordering or removal will affect the final results. However, we take a conservative approach.


*-begin{enumerate}
*-item SNPs that failed differential missingness,  and individuals that have been very poorly genotyped (missingness exceeding 20 per cent) are removed.
*-item Then, SNPs that have been very poorly genotyped (missingness exceeding 20 per cent) are removed.
*-item Finally we select only autosomal SNPs and filter out SNPs  with
*-begin{itemize}
*-item minor allele frequence less than ${params.cut_maf};
*-item individual missingness greater than ${params.cut_mind};
*-item SNP missingness greater than ${params.cut_geno}; and 
*-item HWE p-value less than ${params.cut_hwe}
*-end{itemize}
*-end{enumerate}

*-section{Technical details}

The analysis and report was produced by the h3aGWAS pipeline (*-url{http://github.com/h3abionet/h3agwas}) produced by the Pan-African Bioinformatics Network for H3Africa (*-url{http://www.h3abionet.org}).

The following tools were used:

*-begin{itemize}
*-item PLINK version $plinkversion  [Chang et al 2015]
*-item R version $rversion [R Core Team, 2016]
*-item Nextflow version  $nextflowversion [Di Tommaso et al]

*-end{itemize}

*-section{References}

*-begin{itemize}
*-item Chang, C. C., Chow, C. C., Tellier, L. C., Vattikuti, S., Purcell, S. M., and Lee, J. J. (2015). Second-generation PLINK: rising to the challenge of larger and richer datasets. *-emph{GigaScience}, 4(1), 1-16. *-url{http://doi.org/10.1186/s13742-015-0047-8}
*-item R Core Team (2016). *-emph{R: A language and environment for statistical
  computing}. R Foundation for Statistical Computing, Vienna, Austria.
  *-url{https://www.R-project.org/}
*- Paolo Di Tommaso, Maria Chatzou, Pablo Prieto Baraja, Cedric Notredame. A novel tool for highly scalable computational pipelines. *-url{http://dx.doi.org/10.6084/m9.figshare.1254958}. Nextflow can be downloaded from *url{https://www.nextflow.io/}
*-end{itemize}

*-end{document}'''

template=template.replace("*-",unichr(92))

def countLines(fn):
    count=0
    with open(fn) as f:
        for  line in f:
            count=count+1
    return count


f=open(args.orig)
pdict['numrsnps'] = f.readline().rstrip()
pdict['numrfam']  = f.readline().rstrip()
f.close()



pdict['numhetrem'] =  countLines("$misshetremf")
pdict['numcsnps'] =  countLines(args.cbim)
pdict['numcfam']  =  countLines(args.cfam)
pdict['numdups']  =  countLines(args.dupf)
pdict['numdiffmiss'] = countLines("$diffmiss")

num_fs = countLines(args.fsex)
if num_fs == 1:
    head=open(args.fsex).readline()
    if "No sex" in head: num_fs=0

pdict['numfailedsex']=num_fs
    
out=open("%s.tex"%args.base,"w")
out.write (template%pdict)
out.close()
os.system("pdflatex %s >& /dev/null"%args.base)
os.system("pdflatex %s"%args.base)
  